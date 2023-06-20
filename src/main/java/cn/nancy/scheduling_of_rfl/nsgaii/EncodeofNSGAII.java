package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import java.util.Map;
import cn.nancy.scheduling_of_rfl.Encode;

public class EncodeofNSGAII extends Encode {
    /**
     * 配置编码方法
     */
    private EncodingConfofNSGAII encodingConf;
    /**
     * 工序编码方法
     */
    private EncodingOPofNSGAII encodingOP;

    /**
     * 初始化（不存在可替代机器）
     * @param Machine 每台机器对应的可加工操作
     */
    public EncodeofNSGAII(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, Map<Integer, ArrayList<Integer>> Process, int ProcessNum){
        super(PartsNum, Machine, Process, ProcessNum);
        // 配置编码
        encodingConf(this.encodingConf);
        super.getCode().setConfigurationCode(this.encodingConf.getConfigurationCode());
        // 根据 OptOPSet 获取所有操作的可选工位集合
        SearchOptStageSet(this.encodingConf.getConfigurationCode(), Machine, ProcessNum);
        // 产品操作编码
        for (int p = 0; p < PartsNum; p++) {
            encodingOP(this.encodingConf, this.encodingOP, Process.get(p + 1));
            super.getCode().setOperationCode(p + 1, this.encodingOP.getOperationCode());
        }
    }

    /**
     * 初始化（存在可替代机器）
     * @param Machine 每台机器对应的可加工操作
     */
    public EncodeofNSGAII(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process, Map<Integer, ArrayList<Integer>> AlternativeMachine){
        super(PartsNum, Machine, ProcessNum, Process, AlternativeMachine);
        // 配置编码
        encodingConf(this.encodingConf, AlternativeMachine);
        super.getCode().setConfigurationCode(this.encodingConf.getConfigurationCode());
        // 根据 OptOPSet 获取所有操作的可选工位集合
        SearchOptStageSet(this.encodingConf.getConfigurationCode(), Machine, ProcessNum);
        // 产品操作编码
        for (int p = 0; p < PartsNum; p++) {
            encodingOP(this.encodingConf, this.encodingOP, Process.get(p + 1));
            super.getCode().setOperationCode(p + 1, this.encodingOP.getOperationCode());
        }
    }

    // GET methods
    /**
     * 获取配置编码
     * @return EncodingConf
     */
    public EncodingConfofNSGAII getencodingConf() {
        return this.encodingConf;
    }
    /**
     * 获取工序的工位编码
     * @return EncodingOP
     */
    public EncodingOPofNSGAII getencodingOP() {
        return this.encodingOP;
    }
}
