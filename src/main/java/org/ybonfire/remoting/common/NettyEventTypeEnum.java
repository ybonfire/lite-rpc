package org.ybonfire.remoting.common;

/**
 * NettyEvent类型枚举
 *
 * @author yuanbo
 * @date 2022-12-16 15:29
 */
public enum NettyEventTypeEnum {
    /**
     * Connect
     */
    CONNECT,
    /**
     * Disconnect
     */
    DISCONNECT,
    /**
     * Close
     */
    CLOSE,
    /**
     * Channel Registered
     */
    CHANNEL_REGISTERED,
    /**
     * Channel Unregistered
     */
    CHANNEL_UNREGISTERED,
    /**
     * Channel Active
     */
    CHANNEL_ACTIVE,
    /**
     * Channel Inactive
     */
    CHANNEL_INACTIVE,
    /**
     * User Triggered
     */
    USER_TRIGGERED,
    /**
     * Exception Caught
     */
    EXCEPTION_CAUGHT
}
