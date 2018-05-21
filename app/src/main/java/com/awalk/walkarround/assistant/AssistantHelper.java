package com.awalk.walkarround.assistant;

import com.awalk.walkarround.R;
import com.awalk.walkarround.base.WalkArroundApp;
import com.awalk.walkarround.main.model.ContactInfo;
import com.awalk.walkarround.message.manager.ContactsManager;
import com.awalk.walkarround.util.AppSharedPreference;
import com.awalk.walkarround.util.CommonUtils;

/**
 * Created by Richard on 2018-05-08.
 */

public class AssistantHelper {
    public static final int STEP_INTRODUCE_MYSELF = 0x0001;
    public static final int STEP_INTRODUCE_MYSELF_MASK = ~0x0001;
    public static final int STEP_SEARCHING = 0x0002;
    public static final int STEP_SEARCHING_MASK = ~0x0002;
    public static final int STEP_IM = 0x0004;
    public static final int STEP_IM_MASK = ~0x0004;
    public static final int STEP_IM_SEND_LOC = 0x0008;
    public static final int STEP_IM_SEND_LOC_MASK = ~0x0008;
    public static final int STEP_IM_CLICK_COLOR = 0x0010;
    public static final int STEP_IM_CLICK_COLOR_MASK = ~0x0010;
    public static final int STEP_SHOW_DISTANCE = 0x0020;
    public static final int STEP_SHOW_DISTANCE_MASK = ~0x0020;
    public static final int STEP_COUNT_DOWN = 0x0040;
    public static final int STEP_COUNT_DOWN_MASK = ~0x0040;
    public static final int STEP_EVALUATE = 0x0080;
    public static final int STEP_EVALUATE_MASK = ~0x0080;

    public static final String ASSISTANT_OBJ_ID = "assistant_obj_id";
    public static final String ASSISTANT_USR_NAME = WalkArroundApp.getInstance().getString(R.string.assistant_usr_name);
    public static final String ASSISTANT_SIGNATURE = WalkArroundApp.getInstance().getString(R.string.assistant_signature);


    public static final int STATE_FROM_REGISTER = STEP_INTRODUCE_MYSELF
                                                    | STEP_SEARCHING
                                                    | STEP_IM
                                                    | STEP_IM_SEND_LOC
                                                    | STEP_IM_CLICK_COLOR
                                                    | STEP_SHOW_DISTANCE
                                                    | STEP_COUNT_DOWN
                                                    | STEP_EVALUATE;

    private static volatile AssistantHelper mInstance;
    private AssistantHelper() {

    }

    public static AssistantHelper getInstance() {
        if(mInstance == null) {
            synchronized (AssistantHelper.class) {
                if(mInstance == null) {
                    mInstance = new AssistantHelper();
                }
            }
        }

        return mInstance;
    }

    /**
     * True -- there is guide step.
     * False -- there is no guide step.
     * @return
     */
    public static boolean isThereGuideStep() {
        boolean result = false;

        //0 means we don't need assistant.
        result = (AppSharedPreference.getInt(AppSharedPreference.APP_GUIDE_STATE, 0) != 0);

        return result;
    }

    /**
     * Just for test. Mock a state to test process.
     */
    public void forkRegisterState() {
        AppSharedPreference.putInt(AppSharedPreference.APP_GUIDE_STATE, STATE_FROM_REGISTER);
    }

    /**
     * Note: Mask must from STEP_INTRODUCE_MYSELF_MASK to STEP_EVALUATE_MASK
     * @param mask
     */
    public void updateStepState(int mask) {
        int curValue = AppSharedPreference.getInt(AppSharedPreference.APP_GUIDE_STATE, 0);
        if(curValue != 0) {
            curValue = curValue & mask;
            AppSharedPreference.putInt(AppSharedPreference.APP_GUIDE_STATE, curValue);
        }
    }

    /**
     * Get a step state value by its mask.
     * @param stepValue
     * @return
     */
    public boolean validateStepState(int stepValue) {
        int curValue = AppSharedPreference.getInt(AppSharedPreference.APP_GUIDE_STATE, 0);
        return (curValue & stepValue) == (stepValue);
    }

    public ContactInfo genAssitantContact() {
        ContactInfo assistant = new ContactInfo();
        assistant.setObjectId(ASSISTANT_OBJ_ID);
        assistant.setUsername(ASSISTANT_USR_NAME);
        assistant.setGender(CommonUtils.PROFILE_GENDER_FEMALE);
        assistant.setSignature(ASSISTANT_SIGNATURE);
        assistant.getPortrait().setId(R.drawable.asstant);
        assistant.setBirthday("1998-1-1");

        ContactsManager.getInstance(WalkArroundApp.getInstance().getApplicationContext()).addContactInfo(assistant);

        return assistant;
    }
}
