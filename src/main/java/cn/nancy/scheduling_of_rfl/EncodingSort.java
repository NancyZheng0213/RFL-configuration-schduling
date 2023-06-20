package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Collections;

public class EncodingSort {

    /**
     * 排序编码，如[2,1,3,4]表示工件进入产线的顺序是工件2-工件1-工件3-工件4
     */
    private ArrayList<Integer> SortCode;

    EncodingSort(int PartsNum) {
        // 初始化
        this.SortCode = new ArrayList<Integer>();
        for (int i = 0; i < PartsNum; i++) {
            this.SortCode.add(i + 1);
        }
        // 对SortCode随机排序
        Collections.shuffle(this.SortCode);
    }

    public ArrayList<Integer> getSortCode() {
        return this.SortCode;
    }
}