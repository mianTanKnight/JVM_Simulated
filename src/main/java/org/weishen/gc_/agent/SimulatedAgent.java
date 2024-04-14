package org.weishen.gc_.agent;

import org.weishen.gc_.asm.NewEhASM;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

public class SimulatedAgent implements ClassFileTransformer {

    public static String AGENT_KEY_OBJ = "objAgent";
    public static String AGENT_OBJ_PATH = null;

    public static void premain(String agentArgs, java.lang.instrument.Instrumentation inst) {
        // 加载配置
        EnhancementConfig enhancementConfig = ConfigReader.readConfig(EnhancementConfig.class);
        if (enhancementConfig != null && enhancementConfig.getEnhancement() != null) {
            for (Map<String, String> ek : enhancementConfig.getEnhancement()) {
                String value = ek.get("key");
                if(AGENT_KEY_OBJ.equals(value)) AGENT_OBJ_PATH = ek.get("path");
            }
        }
        inst.addTransformer(new SimulatedAgent());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //
        if (null != AGENT_OBJ_PATH && className.startsWith(AGENT_OBJ_PATH)) {
            try {
                System.out.println("className : " + className  + "Agent for Obj !");
                return NewEhASM.enhanceClass(classfileBuffer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return classfileBuffer;
    }


    public static class EnhancementConfig implements ConfigReader.Config {
        private List<Map<String, String>> enhancement;

        public List<Map<String, String>> getEnhancement() {
            return enhancement;
        }

        public void setEnhancement(List<Map<String, String>> enhancement) {
            this.enhancement = enhancement;
        }
    }
}

