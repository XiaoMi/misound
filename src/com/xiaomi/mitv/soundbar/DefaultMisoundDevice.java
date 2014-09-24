package com.xiaomi.mitv.soundbar;

import android.content.Context;
import com.xiaomi.mitv.soundbar.callback.SoundBarStateTracker2;
import com.xiaomi.mitv.soundbar.gaia.GaiaException;
import com.xiaomi.mitv.soundbar.protocol.UserEQ0x21A;

/**
 * Created by chenxuetong on 9/24/14.
 */
public class DefaultMisoundDevice implements IMiSoundDevice{
    private IMiSoundDevice mAgent;

    public DefaultMisoundDevice(Context context){
        mAgent = SoundBarServiceNative.getInstance(context);
    }

    /**
     * register a callback to listen the mibar status,
     * @param callback the call back
     *
     * {@link SoundBarStateTracker2}:
     * public interface SoundBarStateTracker2 {
     *   void connected(); // BT gaia connected with miBar
     *   void disConnected(); // BT gaia disconnect from miBar
     *   void deviceFound(boolean got, int code); // after miBar scan
     *   void onCommand(int command_id, GaiaCommand result); //got a response of gaia command
     *   }
     */
    @Override
    public void register(SoundBarStateTracker2 callback) {
        mAgent.register(callback);
    }

    /**
     * unregister the callback
     * @param callback
     */
    @Override
    public void unregister(SoundBarStateTracker2 callback) {
        mAgent.unregister(callback);
    }

    /**
     * connect to soundBar, scan it if did not have
     * @return true, connect ok; false failed
     * @throws GaiaException
     */
    @Override
    public boolean connect() throws GaiaException {
        return mAgent.connect();
    }


    /**
     * get the firmware version of MiBar
     * @return version name
     * @throws GaiaException
     */
    @Override
    public String requestModuleVersion() throws GaiaException {
        return mAgent.requestModuleVersion();
    }

    /**
     * get the volume of miBar
     * @return value of [1,31]
     * @throws GaiaException
     */
    @Override
    public int getVolume() throws GaiaException {
        return mAgent.getVolume();
    }

    /**
     * set the volume of miBar with +1 or -1
     * @param way {@link IMiSoundDevice#SOUNDBAR_VOL_INCRESE}SOUNDBAR_VOL_INCRESE or
     *            {@link IMiSoundDevice#SOUNDBAR_VOL_DECRESE}SOUNDBAR_VOL_DECRESE
     * @return true successful, false failed
     * @throws GaiaException
     */
    @Override
    public boolean changeVolume(int way) throws GaiaException {
        return mAgent.changeVolume(way);
    }

    /**
     * check if the sub-woofer is connected with MiBar
     * @param ifNotConnectedForceConnect true, force to connect; false, do nothing
     * @return true, connected; false not connected
     * @throws GaiaException
     */
    @Override
    public boolean isSubWooferConnected(boolean ifNotConnectedForceConnect) throws GaiaException {
        return mAgent.isSubWooferConnected(ifNotConnectedForceConnect);
    }

    /**
     * set the MiBar to safety mode, then it is not scanned via bluetooth
     * @param safety true/false
     * @return true, operation successful; false, failed
     * @throws GaiaException
     */
    @Override
    public boolean setSafetyMod(boolean safety) throws GaiaException {
        return mAgent.setSafetyMod(safety);
    }

    /**
     * check if the MiBar is safety mode
     * @return true/false
     * @throws GaiaException
     */
    @Override
    public boolean getSafetyMode() throws GaiaException {
        return mAgent.getSafetyMode();
    }

    @Override
    public int getVolumeNoOfStep() throws GaiaException {
        return 31;
    }

    /**
     * get the volume of the subwoofer
     * @return value of [1, 31]
     * @throws GaiaException
     */
    @Override
    public int getWooferVolume() throws GaiaException {
        return mAgent.getWooferVolume();
    }

    /**
     * set sub woofer volume with +1 or -1
     * @param way {@link IMiSoundDevice#WOOFER_VOL_INCRESE}WOOFER_VOL_INCRESE or
     *            {@link IMiSoundDevice#WOOFER_VOL_DECRESE}WOOFER_VOL_DECRESE
     * @return
     * @throws GaiaException
     */
    @Override
    public boolean setWooferVolume(byte way) throws GaiaException {
        return mAgent.setWooferVolume(way);
    }

    /**
     * master reset the MiBar settings
     * @return operation result true/false.
     * @throws GaiaException
     */
    @Override
    public boolean masterReset() throws GaiaException {
        return mAgent.masterReset();
    }

    /**
     * check if the MiBar tone voice is mute
     * @return true, the tone is mute, false, the tone is on
     * @throws GaiaException
     */
    @Override
    public boolean getMuteToneVolume() throws GaiaException {
        return mAgent.getMuteToneVolume();
    }

    /**
     * set MiBar tone voice mute flag
     * @param mutemode, true mute the tone, false reset the on
     * @return operation result true/false.
     * @throws GaiaException
     */
    @Override
    public boolean setMuteToneVolume(boolean mutemode) throws GaiaException {
        return mAgent.setMuteToneVolume(mutemode);
    }

    /**
     * connect subWoofer asynchronized, the progress running a while after return still
     * @return operation result true/false.
     * @throws GaiaException
     */
    @Override
    public boolean connectWoofer() throws GaiaException {
        return mAgent.connectWoofer();
    }

    /**
     * get the MiBar eq mode
     * @return {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_COSTUM} customize;
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_STANDARD} standard
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_ROCK} rock
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_CLEAR} clear
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_LIGHT} light
     * @throws GaiaException
     */
    @Override
    public int getEQControl() throws GaiaException {
        return mAgent.getEQControl();
    }

    /**
     * set the MiBar eq mode
     * @param style {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_COSTUM} customize;
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_STANDARD} standard
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_ROCK} rock
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_CLEAR} clear
     *         {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_LIGHT} light
     * @return operation result true/false.
     * @throws GaiaException
     */
    @Override
    public boolean setEQControl(int style) throws GaiaException {
        return mAgent.setEQControl(style);
    }

    /**
     * set custom EQ style for a band {@link com.xiaomi.mitv.soundbar.protocol.UserEQ0x21A#mBand}, valid when
     * {@link #getEQControl()} return {@link com.xiaomi.mitv.soundbar.IMiSoundDevice#EQ_COSTUM}
     * @param eq gain value for a band.{@link com.xiaomi.mitv.soundbar.protocol.UserEQ0x21A}
     * @return operation result true/false.
     * @throws GaiaException
     */
    @Override
    public boolean setUserEQControl(UserEQ0x21A eq) throws GaiaException {
        return mAgent.setUserEQControl(eq);
    }

    /**
     * get current custom EQ style for a band {@link com.xiaomi.mitv.soundbar.protocol.UserEQ0x21A#mBand}
     * @param eq input for the band id {@link com.xiaomi.mitv.soundbar.protocol.UserEQ0x21A#mBand}
     * @return gain value for the band
     * @throws GaiaException
     */
    @Override
    public UserEQ0x21A getUserEQControl(UserEQ0x21A eq) throws GaiaException {
        return mAgent.getUserEQControl(eq);
    }

    /**
     * query the status of MiBar
     * @return status data string
     * @throws GaiaException
     */
    @Override
    public String querySystemTraceInfo() throws GaiaException {
        return mAgent.querySystemTraceInfo();
    }

    /**
     * release the virtual resource of the Mibar in the application client
     */
    @Override
    public void release() {
        mAgent.release();
    }
}
