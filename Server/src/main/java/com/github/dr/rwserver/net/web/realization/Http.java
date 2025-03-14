package com.github.dr.rwserver.net.web.realization;

import com.github.dr.rwserver.net.web.realization.agreement.ShareMessage;
import com.github.dr.rwserver.net.web.realization.constant.Config;
import com.github.dr.rwserver.net.web.realization.constant.HttpCode;
import com.github.dr.rwserver.net.web.realization.i.RequestManager;
import com.github.dr.rwserver.net.web.realization.one.Pond;
import com.github.dr.rwserver.net.web.realization.tools.ShareCon;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Http extends SimpleChannelInboundHandler<Object> implements RequestManager {
    private WebSocketServerHandshaker handshaker;
    private final WebSocketBack webSocketBack;
    private HttpPostRequestDecoder httpDecoder;
    private String uri;
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(true);
    HttpRequest request;

    public Http(WebSocketBack webSocketBack) {
        super(false);
        this.webSocketBack = webSocketBack;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 用户出现异常 ,返回业务异常提醒
        System.out.println("产生异常信息");
        super.exceptionCaught(ctx, cause);
        response(ctx, null, HttpCode.SERVER_ERROR);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;
            uri = request.uri();
            String methodName = request.method().name();
            if ("OPTIONS".equals(methodName)) {
                response(arg0, null, HttpCode.OK);
            } else {
                String contentType = request.headers().get("Content-Type");
                // System.out.println(contentType);
                if (contentType != null && contentType.contains("multipart/form-data")) {
                    if (request.method().equals(HttpMethod.POST)) {
                        httpDecoder = new HttpPostRequestDecoder(factory, request);
                        httpDecoder.setDiscardThreshold(0);
                    } else {
                        // 返回失败
                        response(arg0, null, HttpCode.NOT_FOUND);
                    }
                } else {
                    handleHttpRequest(arg0, (FullHttpRequest) msg);
                }
            }
        }
        if (msg instanceof HttpContent) {
            if (httpDecoder != null) {
                final HttpContent chunk = (HttpContent) msg;
                httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {
                    MyData data = new MyData();
                    List<FileAndName> fileAndNames = writeChunk(arg0);
                    //关闭httpDecoder
                    httpDecoder.destroy();
                    httpDecoder = null;
                    boolean isNotFound = data.my(fileAndNames, this, arg0, uri);
                    if (!isNotFound) {
                        response(arg0, null, HttpCode.NOT_FOUND);
                    }
                }
                ReferenceCountUtil.release(msg);
            }
        }
        if (msg instanceof WebSocketFrame) {
            WebSocketFrame fre = (WebSocketFrame) msg;
            if (fre.content().readableBytes() < 65536) {
                handleWebSocketFrame(arg0, fre, webSocketBack);
            }
        }
    }

    private List<FileAndName> writeChunk(ChannelHandlerContext ctx) throws IOException {
        List<InterfaceHttpData> postList = httpDecoder.getBodyHttpDatas();
        List<FileAndName> fileAndNames = new ArrayList<>();
        for (InterfaceHttpData data : postList) {
            if (data != null) {
                FileAndName fileAndName = new FileAndName();
                fileAndName.setName(data.getName());
                switch (data.getHttpDataType()) {
                    case FileUpload://文件类型
                        final FileUpload fileUpload = (FileUpload) data;
                        InputStream input = new FileInputStream(fileUpload.getFile());
                        fileAndName.setInputStream(input);
                        fileAndNames.add(fileAndName);
                        break;
                    case Attribute://URL 参数
                        Attribute attribute = (Attribute) data;
                        fileAndName.setText(attribute.getValue());
                        fileAndNames.add(fileAndName);
                        break;
                }
            }
        }
        return fileAndNames;
    }

    private void handleHttpRequest(ChannelHandlerContext arg0, FullHttpRequest msg) throws Exception {// !msg.getDecoderResult().isSuccess()
        FullHttpResponse response;
        ShareCon share = new ShareCon();
        MyControl control = new MyControl();
        if (msg.headers().get("Upgrade") == null) {
            if (!msg.decoderResult().isSuccess()) {
                arg0.close();
                return;
            }
            if (msg.content().readableBytes() > Config.getMessage_Max()) {
                arg0.close();
                System.out.println("请求体过长");
                return;
            }
            // 读取POST数据
            ByteBuf fu = msg.content();
            // 创建字节数组
            byte[] body = new byte[fu.readableBytes()];
            // 将缓存区内容读取到字节数组中
            fu.readBytes(body);
            String bodys = new String(body, StandardCharsets.UTF_8);
            ShareMessage message = share.getShareMessage(msg.uri(), bodys);
            boolean bm = control.my(message, this, arg0);
            if (!bm) {
                response(arg0, null, HttpCode.NOT_FOUND);
            }
        } else {
            WebSocketServerHandshakerFactory ws = new WebSocketServerHandshakerFactory(Config.getWebSocketUrl(),
                    null, false);
            handshaker = ws.newHandshaker(msg);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(arg0.channel());
            } else {
                handshaker.handshake(arg0.channel(), msg);
            }
        }
        if (webSocketBack != null) {
            webSocketBack.active(arg0);
        }
    }

    @Override
    public void response(ChannelHandlerContext ch, Object msg, byte httpCode) {
        FullHttpResponse response;
        switch (httpCode) {
            case HttpCode.NOT_FOUND:
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
                break;
            case HttpCode.SERVER_ERROR:
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                //break;
            default:
                byte[] lm = null;
                if (msg != null) {
                    if (msg instanceof String) {
                        lm = msg.toString().getBytes(StandardCharsets.UTF_8);
                    } else {
                        lm = (byte[]) msg;
                    }
                }
                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set("CONTENT_TYPE", "text/html;charset=UTF-8");
                response.headers().set("Access-Control-Allow-Origin", "*");
                response.headers().set("Access-Control-Allow-Headers", "authorization, content-type");
                response.headers().set("Access-Control-Allow-Methods", "POST");

                if (lm != null) {
                    ByteBuf bu = response.content();
                    bu.writeBytes(lm);
                }
                break;
        }
        ch.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ch, WebSocketFrame frame, WebSocketBack webSocketBack) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {// 判断是否是关闭链路的指令
            handshaker.close(ch.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        //String xin = ((TextWebSocketFrame) frame).text();
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
            ByteBuf byteBuf = binaryWebSocketFrame.content();
            byte[] br = new byte[byteBuf.readableBytes()];
            Pond.get().backBuf(ch, br, webSocketBack);
        } else {
            String xin = ((TextWebSocketFrame) frame).text();
            Pond.get().backText(ch, xin, webSocketBack);
        }

    }
}
