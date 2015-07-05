package com.carlocation.comm;

import android.util.Log;

import com.carlocation.comm.messaging.ActionType;
import com.carlocation.comm.messaging.AuthMessage;
import com.carlocation.comm.messaging.BaseMessage;
import com.carlocation.comm.messaging.GlidingPathMessage;
import com.carlocation.comm.messaging.IMTxtMessage;
import com.carlocation.comm.messaging.Location;
import com.carlocation.comm.messaging.LocationCell;
import com.carlocation.comm.messaging.LocationMessage;
import com.carlocation.comm.messaging.MessageResponseStatus;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.RankType;
import com.carlocation.comm.messaging.ResponseMessage;
import com.carlocation.comm.messaging.RestrictedAreaMessage;
import com.carlocation.comm.messaging.StatusMessage;
import com.carlocation.comm.messaging.TaskAssignmentMessage;
import com.carlocation.comm.messaging.TerminalType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by 28851620 on 4/21/2015.
 * @author Jacky Shen
 */
public class UserService implements Serializable{
    private static final long serialVersionUID = 8502706820090766507L;

    private final String LOG_TAG = "UserService";

    //Native Service
    public IMessageService mNativeService;
    public ResponseListener mRspListener;
    public static String mPWD = null;
    public static String mTerminalId = null;

    public UserService(IMessageService service, ResponseListener rspListener) {
        this.mRspListener = rspListener;
        if(service != null){
            this.mNativeService = service;
        }else {
            Log.e(LOG_TAG,"UserService constructor service is null");
        }
    }


    /**
     * Used to send login MSG. The response result will be handled by mRspListener.
     * @param username
     * @param pwd
     */
    public void logIn(String username, String pwd) {
        if (username==null || username.equals("") || pwd==null || pwd.equals("")){
            Log.e(LOG_TAG, "Invalid username: "+ username +"or pwd:" + pwd);
            return;
        }

        this.mTerminalId = username;
        this.mPWD = pwd;

        //Construct a new Login MSG
        AuthMessage authMsg = new AuthMessage(getTransactionId(),getTerminalId(),
                username,pwd, AuthMessage.AuthMsgType.AUTH_LOGIN_MSG);

        // Invoke native service to send message
        Log.d(LOG_TAG,"logIn():Start invoke native service to send message login.");
        sendMessage(authMsg, mRspListener);

    }

    private void sendMessage(BaseMessage msg, ResponseListener rspListener) {
        if (mNativeService == null) {
            Log.e(LOG_TAG,"sendMessage():It seems failed to bind service mNativeService = "+mNativeService);
            mRspListener.onResponse(new Notification(msg, Notification.NotificationType.RESPONSE, Notification.Result.SERVICE_DOWN));
        }else {
            if (rspListener != null){
                mNativeService.sendMessage(msg,rspListener);
            }
            else {
                mNativeService.sendMessage(msg);
            }
        }

    }

    /**
     * Used to send logout MSG. The response result will be handled by mRspListener.
     * @param username
     * @param pwd
     */
    public void logOut(String username, String pwd){
        if (username==null || username.equals("") || pwd==null || pwd.equals("")){
            Log.e(LOG_TAG, "Invalid username: "+ username +"or pwd:" + pwd);
            return;
        }

        //Construct a new AUTH LOGOUT MSG
        AuthMessage authMsg = new AuthMessage(getTransactionId(), getTerminalId(),
                username, pwd, AuthMessage.AuthMsgType.AUTH_LOGOUT_MSG);

        // Invoke native service to send message
        Log.d(LOG_TAG, "logOut():Start invoke native service to send message logout.");
        sendMessage(authMsg, mRspListener);

    }

    /**
     * Used to send my current status(online, leave, offline) to server.
     * @param status
     */
    public void sendMyStatus(StatusMessage.StatusMsgType status){
        StatusMessage statMsg = new StatusMessage(getTransactionId(),getTerminalId(),status);

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendMyStatus():Start invoke native service to send status message.");
        sendMessage(statMsg, null);

    }

    /**
     * Used to send my location to server.
     */
    public void sendMyLocation (){
        LocationMessage myLocationMsg = new LocationMessage(getTransactionId(),getTerminalId(),getMyLocation());

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendMyLocation(): Start invoke native service to send LocationMessage.");
        sendMessage(myLocationMsg, null);
    }

    /**
     * Send IM txt msg to another terminal.
     * @param toTerminal    Terminal IDs of destination
     * @param rank          Rank of Msg
     * @param content       Content of msg
     */
    public void sendImTxtMsg(List<String> toTerminal, RankType rank, String content){
        IMTxtMessage txtMessage = new IMTxtMessage(getTransactionId(),getTerminalId(),toTerminal,rank,content);

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendImTxtMsg(): Start invoke native service to send IM txt msg.");
        sendMessage(txtMessage, null);
    }

    /**
     * Send IM voice msg to another terminal.
     * @param toTerminal    Terminal ID of destination
     * @param voiceData     Context of voice data
     */
    /*public void sendImVoiceMsg(List<String> toTerminal, byte[] voiceData){
        IMVoiceMessage voiceMessage = new IMVoiceMessage(getTransactionId(),
                getTerminalId(),toTerminal,voiceData);

        // Invoke native service to send message
        Log.d(LOG_TAG, "sendImVoiceMsg(): Start invoke native service to send IM voice msg.");
        if(mNativeService!= null){
            mNativeService.sendMessage(voiceMessage);
        }else {
            Log.e(LOG_TAG,"sendImVoiceMsg():It seems failed to bind service mNativeService = "+mNativeService);
        }

    }*/

    /**
     * send to schedule server to indicate starting to execute given <>taskID</>
     * @param taskId
     */
    public void startWorkMsg(short taskId){
        TaskAssignmentMessage startWorkMsg  = new TaskAssignmentMessage(getTransactionId(),
                getTerminalId(), ActionType.ACTION_START,taskId,null);

        // Invoke native service to send message
        Log.d(LOG_TAG, "startWork(): Start invoke native service to send start work msg.");
        sendMessage(startWorkMsg, mRspListener);

    }

    /**
     * Send to schedule server to indicate having finished the given <>taskID</>
     * @param taskId
     */
    public void finishWorkMsg(short taskId){
        TaskAssignmentMessage finishWorkMsg = new TaskAssignmentMessage(getTransactionId(),getTerminalId(),ActionType.ACTION_FINISH,taskId,null);

        // Invoke native service to send message
        Log.d(LOG_TAG, "finishWork(): Start invoke native service to send finish work msg.");
        sendMessage(finishWorkMsg, mRspListener);

    }

    /**
     * Send to schedule server to query work ID
     * @param taskId
     */
    public void queryWorkById(short taskId){
        TaskAssignmentMessage queryWorkMsg = new TaskAssignmentMessage(getTransactionId(),
                getTerminalId(),ActionType.ACTION_QUERY,taskId,null);

        // Invoke native service to send message
        Log.d(LOG_TAG, "queryWorkMsg(): Start invoke native service to query work by id msg.");
        sendMessage(queryWorkMsg, mRspListener);

    }

    /**
     * Used to query gliding path by ID from server.
     * @param glidePathId
     */
    void queryGlidePathById(int glidePathId){
        GlidingPathMessage queryPathMsg = new GlidingPathMessage(getTransactionId(),
                ActionType.ACTION_QUERY,getTerminalId(),null,glidePathId,null);

        // Invoke native service to send message
        Log.d(LOG_TAG, "queryPathMsg(): Start invoke native service to query glide path by id msg.");
        sendMessage(queryPathMsg, mRspListener);
    }

    /**
     * Used to query restricted area by ID from server.
     * @param warnAreaId
     */
    void queryWarnAreaById(int warnAreaId){
        RestrictedAreaMessage queryWarnMsg = new RestrictedAreaMessage(getTransactionId(),
                getTerminalId(),ActionType.ACTION_QUERY,warnAreaId,null);

        // Invoke native service to send message
        Log.d(LOG_TAG, "queryWarnMsg(): Start invoke native service to query warn area by id msg.");
        sendMessage(queryWarnMsg, mRspListener);
    }

    void responActionAssign(BaseMessage message, MessageResponseStatus status){
        ResponseMessage rspMsg = new ResponseMessage(message,status);
        // Invoke native service to send message
        Log.d(LOG_TAG, "responActionAssign(): Start invoke native service to send rsp of assignment to server.");
        if(mNativeService!= null){
            mNativeService.sendMessageResponse(rspMsg);
        }else {
            Log.e(LOG_TAG,"responActionAssign():It seems failed to bind service mNativeService = "+mNativeService);
        }
    }


    /**
     * Used to produce transaction id for send msg.
     * @return
     */
    public static long getTransactionId(){
        //FIXME Get TransactionID
        return (new Random().nextLong());
    }

    /**
     * Used to get terminal ID from property.
     * @return
     */
    public static String getTerminalId (){
        return mTerminalId;
    }

    /**
     * Used to get terminal type from property.
     * @return
     */
    public static TerminalType getTerminalType(){
        //FIXME Get terminal ID from property
        return TerminalType.TERMINAL_CAR;
    }

    /**
     * Used to retrieve my location from BeiDou positioning system.
     * @return
     */
    public static ArrayList<LocationCell> getMyLocation(){
        //FIXME Get Location from BeiDou positioning system
        ArrayList<LocationCell> myLocation  = new ArrayList<LocationCell>();

        Location loc = new Location(111.111,222.222);
        LocationCell myCell = new LocationCell(getTerminalId(),TerminalType.TERMINAL_CAR,loc,getMySpeed());

        myLocation.add(myCell);

        return myLocation;
    }


    /**
     * Used to retrieve my speed from BeiDou positioning system.
     * @return
     */
    public static float getMySpeed(){
        //FIXME Get my speed from BeiDou positioning system
        float mySpeed = 12.23f;
        return mySpeed;
    }

}
