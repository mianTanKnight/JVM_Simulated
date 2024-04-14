package org.weishen.gc_.gcm;


import org.weishen.gc_.obj_.SimulatedObj;

import java.util.ArrayList;

public interface SimulatedGC {

    ArrayList<SimulatedObj> getRootObjs();

    long safeTime();

}
