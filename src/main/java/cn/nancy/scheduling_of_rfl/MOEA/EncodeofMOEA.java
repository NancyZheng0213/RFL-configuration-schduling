package cn.nancy.scheduling_of_rfl.MOEA;

import java.util.ArrayList;
import java.util.Map;

import cn.nancy.scheduling_of_rfl.Encode;

public class EncodeofMOEA extends Encode {
    private EncodingConfofMOEA encodingConf;
    private EncodingOPofMOEA encodingOP;

    /**
     * 初始化（不存在可替代机器）
     * @param PartNum 工件种类数量
     * @param Machine 每台机器对应的可加工操作
     * @param ProcessNum 工序数量
     * @param Process 每种工件对应的加工工序，<code>Map&lt;Integer, ArrayList&gt;</code>
     */
    public EncodeofMOEA(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process) {
        super(PartsNum, Machine, ProcessNum);
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
     * @param PartNum 工件种类数量
     * @param Machine 每台机器对应的可加工操作
     * @param ProcessNum 工序数量
     * @param Process 每种工件对应的加工工序列表，<code>Map&lt;Integer, ArrayList&gt;</code>
     * @param AlternativeMachine 每台设备对应的可替换设备列表，<code>Map&lt;Integer, ArrayList&gt;</code>
     */
    public EncodeofMOEA(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process, Map<Integer, ArrayList<Integer>> AlternativeMachine) {
        super(PartsNum, Machine, ProcessNum);
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
    public EncodingConfofMOEA getencodingConf() {
        return this.encodingConf;
    }
    /**
     * 获取工序的工位编码
     * @return EncodingOP
     */
    public EncodingOPofMOEA getencodingOP() {
        return this.encodingOP;
    }
}