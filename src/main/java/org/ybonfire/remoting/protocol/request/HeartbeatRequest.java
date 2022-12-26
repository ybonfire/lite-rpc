package org.ybonfire.remoting.protocol.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 心跳请求
 *
 * @author yuanbo
 * @date 2022-12-21 18:21
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HeartbeatRequest implements Serializable {
    /**
     * 客户端Id
     */
    private String clientId;
    /**
     * 连接Id
     */
    private String connectionId;
    /**
     * 请求间隔
     */
    private Long intervalMillis;
}
