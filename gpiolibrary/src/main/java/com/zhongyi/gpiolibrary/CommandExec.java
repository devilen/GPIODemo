package com.zhongyi.gpiolibrary;

import android.text.TextUtils;
import android.util.Log;

import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 命令行执行工具
 */
public class CommandExec {

    private CommandExec() {
    }

    /**
     * 修改gpio下文件的权限，让他可以使用
     * @param ledId gpio 编号
     * @return 成功true
     */
    public static boolean execRedLed(int ledId) {
        return execCommand(String.format("cd /sys/class/gpio/gpio%d/ && chmod 777 value", ledId));
    }

    /**
     * 还原 gpio文件的权限
     * @param ledId gpio 编号
     * @return 成功 true
     */
    public static boolean releaseLed(int ledId) {
        return execCommand(String.format("cd /sys/class/gpio/gpio%d/ && chmod 644 value", ledId));
    }


    public static boolean execCommand(String command) {
        boolean status = false;
        if (TextUtils.isEmpty(command)) {
            return status;
        }
        try {
            Process exec = Runtime.getRuntime().exec("su");
            OutputStream outputStream = exec.getOutputStream();
            outputStream.write(command.getBytes(Charset.forName("utf-8")));
            outputStream.write("\n".getBytes());
            outputStream.write("exit\n".getBytes());
            outputStream.flush();
            int waitFor = exec.waitFor();
            Log.e("execCommand", "execCommand command:"+command+";waitFor=" + waitFor);
            if (waitFor == 0) {
                //chmod succeed
                status = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("execCommand", "execCommand exception=" + e.getMessage());
            return false;
        }
        return status;
    }
}
