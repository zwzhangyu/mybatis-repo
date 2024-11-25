package com.zy.client.plugin.utils;

import java.io.Closeable;
import java.io.IOException;

/*******************************************************
 * Created by ZhangYu on 2024/11/24
 * Description :
 * History   :
 *******************************************************/
public abstract class IOUtils {

  public static void closeQuietly(Closeable closeable) {
    if(closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void closeQuietly(AutoCloseable closeable) {
    if(closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


}
