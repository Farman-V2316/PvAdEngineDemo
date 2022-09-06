package com.newshunt.adengine.util.permission;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kush Saini on 06/09/22
 * Last Modified by Kush Saini on 06/09/22
 * Description:
 */
public class PermissionUtils {

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED;
    }

    public static String getUsesPermission() {
        Map<String, Boolean> usesPermission = new HashMap<>();
        for (Permission permission : Permission.values()) {
            if(Permission.INVALID == permission){
                continue;
            }
            usesPermission.put(permission.name(),
                hasPermission(CommonUtils.getApplication(), permission.getPermission()));
        }
        return JsonUtils.toJson(usesPermission);
    }
}
