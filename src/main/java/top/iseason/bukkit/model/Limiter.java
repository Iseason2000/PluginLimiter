package top.iseason.bukkit.model;

import lombok.Data;

/**
 * 用于检测是否触发限制条件
 */
@Data
public class Limiter {
    private String pluginName;
    private boolean enabled = false;

}

