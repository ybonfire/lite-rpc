package org.ybonfire.remoting.protocol.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 心跳响应
 *
 * @author yuanbo
 * @date 2022-12-23 10:31
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HeartbeatResponse implements Serializable {
    /**
     * 连接Id
     */
    private String connectionId;
    /**
     * 接收时间戳
     */
    private Long receiveTimestamp;
}
