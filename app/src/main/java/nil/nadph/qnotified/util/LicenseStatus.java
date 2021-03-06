/* QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2020 cinit@github.com
 * https://github.com/cinit/QNotified
 *
 * This software is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
package nil.nadph.qnotified.util;

import nil.nadph.qnotified.ExfriendManager;
import nil.nadph.qnotified.chiral.MdlMolParser;
import nil.nadph.qnotified.chiral.Molecule;
import nil.nadph.qnotified.config.ConfigManager;

import java.io.IOException;
import java.util.HashSet;

import static nil.nadph.qnotified.util.Utils.log;

public class LicenseStatus {
    public static final String qn_eula_status = "qh_eula_status";//typo, ignore it
    public static final String qn_auth2_molecule = "qn_auth2_molecule";
    public static final String qn_auth2_chiral = "qn_auth2_chiral";

    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_ACCEPT = 1;
    public static final int STATUS_DENIAL = 2;

    private static Molecule mAuth2Mol = null;
    private static int[] mAuth2Chiral = null;

    public static int getEulaStatus() {
        return ConfigManager.getDefaultConfig().getIntOrDefault(qn_eula_status, 0);
    }

    public static void setEulaStatus(int status) {
        ConfigManager.getDefaultConfig().putInt(qn_eula_status, status);
        try {
            ConfigManager.getDefaultConfig().save();
        } catch (IOException e) {
            log(e);
            Utils.showErrorToastAnywhere(e.toString());
        }
    }

    public static boolean getAuth2Status() {
        if ((getCurrentUserWhiteFlags() & UserFlagConst.WF_BYPASS_AUTH_2) != 0) return true;
        return getAuth2Chiral() != null && getAuth2Molecule() != null;
    }

    @Nullable
    public static int[] getAuth2Chiral() {
        if (mAuth2Chiral == null) {
            try {
                String chirals = ConfigManager.getDefaultConfig().getString(qn_auth2_chiral);
                if (chirals != null && chirals.length() > 0) {
                    HashSet<Integer> ch = new HashSet<>();
                    for (String s : chirals.split(",")) {
                        if (s.length() > 0) ch.add(Integer.parseInt(s));
                    }
                    mAuth2Chiral = Utils.integerSetToArray(ch);
                }
            } catch (Exception e) {
                log(e);
            }
        }
        return mAuth2Chiral;
    }

    @Nullable
    public static Molecule getAuth2Molecule() {
        if (mAuth2Mol == null) {
            try {
                String mdlmol = ConfigManager.getDefaultConfig().getString(qn_auth2_molecule);
                if (mdlmol != null && mdlmol.length() > 0) {
                    mAuth2Mol = MdlMolParser.parseString(mdlmol);
                }
            } catch (Exception e) {
                log(e);
            }
        }
        return mAuth2Mol;
    }

    public static void clearAuth2Status() {
        mAuth2Chiral = null;
        mAuth2Mol = null;
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        cfg.remove(qn_auth2_molecule);
        cfg.remove(qn_auth2_chiral);
        try {
            cfg.save();
        } catch (IOException e) {
            log(e);
            Utils.showErrorToastAnywhere(e.toString());
        }
    }

    public static void setAuth2Status(Molecule mol, int[] chiral) {
        mAuth2Mol = mol;
        mAuth2Chiral = chiral;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chiral.length; i++) {
            if (i != 0) sb.append(',');
            sb.append(chiral[i]);
        }
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        cfg.putString(qn_auth2_molecule, mol.toMdlMolString());
        cfg.putString(qn_auth2_chiral, sb.toString());
        try {
            cfg.save();
        } catch (IOException e) {
            log(e);
            Utils.showErrorToastAnywhere(e.toString());
        }
    }

    public static boolean hasUserAgreeEula() {
        return getEulaStatus() == STATUS_ACCEPT;
    }

    public static volatile boolean sDisableCommonHooks = false;

    public static boolean isBlacklisted() {
        return (getCurrentUserBlackFlags() & UserFlagConst.BF_REJECT) != 0;
    }

    public static boolean isLoadingDisabled() {
        return (getCurrentUserBlackFlags() & UserFlagConst.BF_SILENT_DISABLE_LOAD) != 0;
    }

    public static boolean isSilentGone() {
        return (getCurrentUserBlackFlags() & UserFlagConst.BF_SILENT_GONE) != 0;
    }

    public static boolean isBypassAuth2() {
        return (getCurrentUserWhiteFlags() & UserFlagConst.WF_BYPASS_AUTH_2) != 0;
    }

    public static final String qn_auth_uin_black_flags = "qn_auth_uin_black_flags";
    public static final String qn_auth_uin_white_flags = "qn_auth_uin_white_flags";
    public static final String qn_auth_uin_update_time = "qn_auth_uin_update_time";

    public static int getCurrentUserBlackFlags() {
        long uin = Utils.getLongAccountUin();
        if (uin < 10000) return 0;
        ExfriendManager exm = ExfriendManager.get(uin);
        return exm.getIntOrDefault(qn_auth_uin_black_flags, 0);
    }

    public static int getCurrentUserWhiteFlags() {
        long uin = Utils.getLongAccountUin();
        if (uin < 10000) return 0;
        ExfriendManager exm = ExfriendManager.get(uin);
        return exm.getIntOrDefault(qn_auth_uin_white_flags, 0);
    }
}
