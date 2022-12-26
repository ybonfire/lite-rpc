package org.ybonfire.remoting;

/**
 * 远程命令类型枚举
 *
 * @author yuanbo
 * @date 2022-12-15 16:56
 */
public enum RemotingCommandTypeEnum {
    /**
     * request
     */
    REQUEST(0, "request"),
    /**
     * response
     */
    RESPONSE(1, "response");

    private final int code;
    private final String description;

    RemotingCommandTypeEnum(final int code, final String description) {
        this.code = code;
        this.description = description;
    }
}
