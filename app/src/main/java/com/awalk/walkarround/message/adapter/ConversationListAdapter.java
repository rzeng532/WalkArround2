package com.awalk.walkarround.message.adapter;

import android.content.Context;
import com.awalk.walkarround.message.model.MessageSessionBaseModel;
import com.awalk.walkarround.message.util.MessageConstant.ConversationType;
import com.awalk.walkarround.message.util.MessageConstant.TopState;
import com.awalk.walkarround.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConversationListAdapter extends BaseConversationListAdapter {
    public static final int FIXED_ENTRANCES = 3;
    private static final Logger logger = Logger.getLogger(ConversationListAdapter.class.getSimpleName());

    public ConversationListAdapter(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * 是否全选中了
     *
     * @return
     */
    @Override
    public boolean isSelectAll() {
        return getChosenItemCount() == (getCount() - FIXED_ENTRANCES);
    }

    /**
     * 全选
     */
    @Override
    public void setSelectAll() {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (canSelectable(getItem(i))) {
                addToChosenPositionList(i);
            }
        }
    }

}
