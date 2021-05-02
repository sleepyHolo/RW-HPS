package com.github.dr.rwserver.net.web.realization.tools;

public class HttpIdCreator {
    // 生成分布式全局唯一ID
    public static final long sequenceBits = 12L;// 序列掩码位数
    public static final long workBits = 4L;// 分布式机器号共四位
    public static final long workId = 1L;
    public static final long workAndBusBites = workBits + sequenceBits;
    private static final long sequenceMask = ~(-1L << sequenceBits);// 序列掩码极值
    private static final HttpIdCreator AcceptorId = new HttpIdCreator();
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    private HttpIdCreator() {

    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return (timestamp << workAndBusBites) | (sequence << workBits) | workId;

    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    public static HttpIdCreator get() {
        return AcceptorId;
    }
}
