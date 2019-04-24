/**
 * Copyright (C) 2011 Michael Mosmann <michael@mosmann.de> Martin Jöhren
 * <m.joehren@googlemail.com>
 *
 * with contributions from konstantin-ba@github, Archimedes Trajano
 * (trajano@github), Kevin D. Keck (kdkeck@github), Ben McCann
 * (benmccann@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.sdkit.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.lang.model.SourceVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

public abstract class Processes {
  private static final Logger logger = LogManager.getLogger();

  private static final PidHelper PID_HELPER;

  static {
    // Comparing with the string value to avoid a strong dependency on JDK 9
    if (SourceVersion.latest().toString().equals("RELEASE_9")) { //$NON-NLS-1$
      PID_HELPER = PidHelper.JDK_9;
    } else {
      PID_HELPER = PidHelper.LEGACY;
    }
  }

  private Processes() {
    // no instance
  }

  public static Long processId(Process process) {
    return PID_HELPER.getPid(process);
  }

  private static Long unixLikeProcessId(Process process) {
    Class<?> clazz = process.getClass();
    try {
      if (clazz.getName().equals("java.lang.UNIXProcess")) { //$NON-NLS-1$
        Field pidField = clazz.getDeclaredField("pid"); //$NON-NLS-1$
        pidField.setAccessible(true);
        Object value = pidField.get(process);
        if (value instanceof Integer) {
          logger.debug("Detected pid: {}", value); //$NON-NLS-1$
          return ((Integer) value).longValue();
        }
      }
    } catch (SecurityException sx) {
      sx.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @see http://www.golesny.de/p/code/javagetpid
   *
   * @return
   */
  private static Long windowsProcessId(Process process) {
    if (process.getClass().getName().equals("java.lang.Win32Process") //$NON-NLS-1$
        || process.getClass().getName().equals("java.lang.ProcessImpl")) { //$NON-NLS-1$
      /* determine the pid on windows plattforms */
      try {
        Field f = process.getClass().getDeclaredField("handle"); //$NON-NLS-1$
        f.setAccessible(true);
        long handl = f.getLong(process);

        Kernel32 kernel = Kernel32.INSTANCE;
        WinNT.HANDLE handle = new WinNT.HANDLE();
        handle.setPointer(Pointer.createConstant(handl));
        int ret = kernel.GetProcessId(handle);
        logger.debug("Detected pid: {}", ret); //$NON-NLS-1$
        return Long.valueOf(ret);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private enum PidHelper {

    JDK_9 {
      @Override
      Long getPid(Process process) {
        try {
          // Invoking via reflection to avoid a strong dependency on JDK 9
          Method getPid = Process.class.getMethod("getPid"); //$NON-NLS-1$
          return (Long) getPid.invoke(process);
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    },
    LEGACY {
      @Override
      Long getPid(Process process) {
        Long pid = unixLikeProcessId(process);
        if (pid == null) {
          pid = windowsProcessId(process);
        }
        return pid;
      }
    };

    abstract Long getPid(Process process);
  }
}
