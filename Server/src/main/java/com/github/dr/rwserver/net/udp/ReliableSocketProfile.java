package com.github.dr.rwserver.net.udp;


/**
 * This class specifies the RUDP parameters of a socket.
 *
 * @author Adrian Granados
 * @see    com.github.dr.rwserver.net.udp.ReliableSocket
 */
public class ReliableSocketProfile {
    public final static int MAX_SEND_QUEUE_SIZE    = 32;
    public final static int MAX_RECV_QUEUE_SIZE    = 32;

    public final static int MAX_SEGMENT_SIZE       = 128;
    public final static int MAX_OUTSTANDING_SEGS   = 3;
    public final static int MAX_RETRANS            = 3;
    public final static int MAX_CUMULATIVE_ACKS    = 3;
    public final static int MAX_OUT_OF_SEQUENCE    = 3;
    public final static int MAX_AUTO_RESET         = 3;
    public final static int NULL_SEGMENT_TIMEOUT   = 2000;
    public final static int RETRANSMISSION_TIMEOUT = 600;
    public final static int CUMULATIVE_ACK_TIMEOUT = 300;

    /**
     * Creates a profile with the default RUDP parameter values.
     *
     * Note: According to the RUDP protocol's draft, the default
     * maximum number of retransmissions is 3. However, if packet
     * drops are too high, the connection may get stall unless
     * the sender continues to retransmit packets that have not been
     * unacknowledged. We will use 0 instead, which means unlimited.
     *
     */
    public ReliableSocketProfile() {
        this(MAX_SEND_QUEUE_SIZE,
             MAX_RECV_QUEUE_SIZE,
             MAX_SEGMENT_SIZE,
             MAX_OUTSTANDING_SEGS,
             0/*MAX_RETRANS*/,
             MAX_CUMULATIVE_ACKS,
             MAX_OUT_OF_SEQUENCE,
             MAX_AUTO_RESET,
             NULL_SEGMENT_TIMEOUT,
             RETRANSMISSION_TIMEOUT,
             CUMULATIVE_ACK_TIMEOUT);
    }

    /**
     * Creates an profile with the specified RUDP parameter values.
     *
     * @param maxSendQueueSize      maximum send queue size (packets).
     * @param maxRecvQueueSize      maximum receive queue size (packets).
     * @param maxSegmentSize        maximum segment size (octets) (must be at least 22).
     * @param maxOutstandingSegs    maximum number of outstanding segments.
     * @param maxRetrans            maximum number of consecutive retransmissions (0 means unlimited).
     * @param maxCumulativeAcks     maximum number of unacknowledged received segments.
     * @param maxOutOfSequence      maximum number of out-of-sequence received segments.
     * @param maxAutoReset          maximum number of consecutive auto resets (not used).
     * @param nullSegmentTimeout    null segment timeout (ms).
     * @param retransmissionTimeout retransmission timeout (ms).
     * @param cumulativeAckTimeout  cumulative acknowledge timeout (ms).
     */
    public ReliableSocketProfile(int maxSendQueueSize,
                                 int maxRecvQueueSize,
                                 int maxSegmentSize,
                                 int maxOutstandingSegs,
                                 int maxRetrans,
                                 int maxCumulativeAcks,
                                 int maxOutOfSequence,
                                 int maxAutoReset,
                                 int nullSegmentTimeout,
                                 int retransmissionTimeout,
                                 int cumulativeAckTimeout) {
        checkValue("maxSendQueueSize",      maxSendQueueSize,      1,   255);
        checkValue("maxRecvQueueSize",      maxRecvQueueSize,      1,   255);
        checkValue("maxSegmentSize",        maxSegmentSize,        22,  65535);
        checkValue("maxOutstandingSegs",    maxOutstandingSegs,    1,   255);
        checkValue("maxRetrans",            maxRetrans,            0,   255);
        checkValue("maxCumulativeAcks",     maxCumulativeAcks,     0,   255);
        checkValue("maxOutOfSequence",      maxOutOfSequence,      0,   255);
        checkValue("maxAutoReset",          maxAutoReset,          0,   255);
        checkValue("nullSegmentTimeout",    nullSegmentTimeout,    0,   65535);
        checkValue("retransmissionTimeout", retransmissionTimeout, 100, 65535);
        checkValue("cumulativeAckTimeout",  cumulativeAckTimeout,  100, 65535);

        _maxSendQueueSize      = maxSendQueueSize;
        _maxRecvQueueSize      = maxRecvQueueSize;
        _maxSegmentSize        = maxSegmentSize;
        _maxOutstandingSegs    = maxOutstandingSegs;
        _maxRetrans            = maxRetrans;
        _maxCumulativeAcks     = maxCumulativeAcks;
        _maxOutOfSequence      = maxOutOfSequence;
        _maxAutoReset          = maxAutoReset;
        _nullSegmentTimeout    = nullSegmentTimeout;
        _retransmissionTimeout = retransmissionTimeout;
        _cumulativeAckTimeout  = cumulativeAckTimeout;
    }

    /**
     * Returns the maximum send queue size (packets).
     */
    public int maxSendQueueSize() {
        return _maxSendQueueSize;
    }

    /**
     * Returns the maximum receive queue size (packets).
     */
    public int maxRecvQueueSize() {
        return _maxRecvQueueSize;
    }

    /**
     * Returns the maximum segment size (octets).
     */
    public int maxSegmentSize() {
        return _maxSegmentSize;
    }

    /**
     * Returns the maximum number of outstanding segments.
     */
    public int maxOutstandingSegs() {
        return _maxOutstandingSegs;
    }

    /**
     * Returns the maximum number of consecutive retransmissions (0 means unlimited).
     */
    public int maxRetrans() {
        return _maxRetrans;
    }

    /**
     * Returns the maximum number of unacknowledged received segments.
     */
    public int maxCumulativeAcks() {
        return _maxCumulativeAcks;
    }

    /**
     * Returns the maximum number of out-of-sequence received segments.
     */
    public int maxOutOfSequence() {
        return _maxOutOfSequence;
    }

    /**
     * Returns the maximum number of consecutive auto resets.
     */
    public int maxAutoReset() {
        return _maxAutoReset;
    }

    /**
     * Returns the null segment timeout (ms).
     */
    public int nullSegmentTimeout() {
        return _nullSegmentTimeout;
    }

    /**
     * Returns the retransmission timeout (ms).
     */
    public int retransmissionTimeout() {
        return _retransmissionTimeout;
    }

    /**
     * Returns the cumulative acknowledge timeout (ms).
     */
    public int cumulativeAckTimeout() {
        return _cumulativeAckTimeout;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(_maxSendQueueSize).append(", ");
        sb.append(_maxRecvQueueSize).append(", ");
        sb.append(_maxSegmentSize).append(", ");
        sb.append(_maxOutstandingSegs).append(", ");
        sb.append(_maxRetrans).append(", ");
        sb.append(_maxCumulativeAcks).append(", ");
        sb.append(_maxOutOfSequence).append(", ");
        sb.append(_maxAutoReset).append(", ");
        sb.append(_nullSegmentTimeout).append(", ");
        sb.append(_retransmissionTimeout).append(", ");
        sb.append(_cumulativeAckTimeout);
        sb.append("]");
        return sb.toString();
    }

    private void checkValue(String param,
                                 int value,
                                 int minValue,
                                 int maxValue) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(param);
        }
    }

    private final int _maxSendQueueSize;
    private final int _maxRecvQueueSize;
    private final int _maxSegmentSize;
    private final int _maxOutstandingSegs;
    private final int _maxRetrans;
    private final int _maxCumulativeAcks;
    private final int _maxOutOfSequence;
    private final int _maxAutoReset;
    private final int _nullSegmentTimeout;
    private final int _retransmissionTimeout;
    private final int _cumulativeAckTimeout;
}
