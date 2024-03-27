package org.weishen.gc_.gcm_;


import jakarta.annotation.Nullable;
import org.weishen.gc_.obj_.SimulatedObject;

import java.util.ArrayList;

public interface SimulatedGC {

    @Nullable
    ArrayList<SimulatedObject> getRootObjs();

    long safeTime();


//    List<>






}
