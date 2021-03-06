package com.carlocation.demo;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.carlocation.comm.ConnectionState;
import com.carlocation.comm.IMessageService;
import com.carlocation.comm.MessageService;
import com.carlocation.comm.NativeServInterface;
import com.carlocation.comm.NotificationListener;
import com.carlocation.comm.ResponseListener;
import com.carlocation.comm.UserService;
import com.carlocation.comm.messaging.IMMessage;
import com.carlocation.comm.messaging.IMTxtMessage;
import com.carlocation.comm.messaging.LocationCell;
import com.carlocation.comm.messaging.MessageType;
import com.carlocation.comm.messaging.Notification;
import com.carlocation.comm.messaging.RankType;
import com.carlocation.comm.messaging.TerminalType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ListActivity implements NativeServInterface {
    private final static String LOG_TAG = "MainActivity";

    /**
     * Notification listener
     */
    private LocalListener mListener;
    /**
     * Native Service
     */
    private IMessageService mNativeService;
    /**
     * User Service
     */
    private UserService mUserService;

    /**
     * Record current connection state
     */
    private ConnectionState mConnState = ConnectionState.NONE;

    /**
     * An indicator to indicate if this MainActivity still alive (not destroyed).
     */
    private boolean mResumed;

    private static List<String> mItems;
    private ListView mList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getUserService();
        regNotifictListenner();
        regConnStateRecver();

        mList = getListView();
        mItems = new ArrayList<>();

        //Reflect all announced public methods in UserService
        try{
            Method[] methods = UserService.class.getDeclaredMethods();
            for (int i=0;i<methods.length;i++) {
                mItems.add(methods[i].getName());
            }
        }catch (SecurityException e){
            Log.e(LOG_TAG, "onCreate():NoSuchMethodException!");
            e.printStackTrace();
        }

        setListAdapter(new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_list_item_1,mItems));

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0:
                        //finishWorkMsg
                        break;
                    case 1:
                        //login
                        break;
                    case 2:
                        //logout
                        break;
                    case 3:
                        //QueryGlidePathById
                        break;
                    case 4:
                        //QueryWarnAreaById
                        break;
                    case 5:
                        //QueryWorkById
                        break;
                    case 6:
                        //ResponseActionAssign
                        break;
                    case 7:
                        //Send ImTxtMsg
                        if (mConnState == ConnectionState.CONNECTED){
                            List<String> toId = new ArrayList<>();
                            String toTerminalId;
                            if (mUserService.getTerminalId().equals("t1")) {
                                toTerminalId = "t2";
                            }else {
                                toTerminalId = "t1";
                            }
                            toId.add(toTerminalId);
                            mUserService.sendImTxtMsg(toId, RankType.NORMAL,"Hello!I'm "+mUserService.getTerminalId());
                        } else {
                            Toast.makeText(MainActivity.this,"No connection for rabbitmq",Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case 8:
                        //sendMyLocation
                        break;
                    case 9:
                        //sendMyStatus
                        break;
                    case 10:
                        //startWorkMsg
                        break;
                    case 11:
                        //getMyLocation
                        ArrayList<LocationCell> myLoc = mUserService.getMyLocation();
                        Toast.makeText(MainActivity.this,myLoc.toString(),Toast.LENGTH_LONG).show();
                        break;
                    case 12:
                        //getMySpeed
                        float speed = mUserService.getMySpeed();
                        Toast.makeText(MainActivity.this,Float.toString(speed),Toast.LENGTH_SHORT).show();
                        break;
                    case 13:
                        //getTerminalId()
                        String tId  = mUserService.getTerminalId();
                        Toast.makeText(MainActivity.this, tId, Toast.LENGTH_SHORT).show();
                        break;
                    case 14:
                        //getTerminalType
                        TerminalType tType = mUserService.getTerminalType();
                        Toast.makeText(MainActivity.this,tType.toString(),Toast.LENGTH_SHORT).show();
                        break;
                    case 15:
                        //getTransactionId
                        break;
                    default:break;
                }
            }
        });


    }

    private void regConnStateRecver() {
        //Get current rabbitMq connection state
        mConnState = mNativeService.getConnState();

        //"mConnStateReceiver" used to recv connection state notification
        IntentFilter filter = new IntentFilter();
        filter.addAction(MessageService.BROADCAST_ACTION_STATE_CHANGED);
        filter.addCategory(MessageService.BROADCAST_CATEGORY);
        registerReceiver(mConnStateReceiver, filter);
    }

    private BroadcastReceiver mConnStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action  = intent.getAction();
            //Use 'if' for the purpose of further extending filter more actions.
            if (MessageService.BROADCAST_ACTION_STATE_CHANGED.equals(action)) {
                ConnectionState newState = (ConnectionState)intent.getSerializableExtra(MessageService.EXTRA_CONNECTION_STATE);
                if (mConnState!= newState) {
                    Log.d(LOG_TAG,"mConnStateReceiver(): Connection state changed [old:"
                            +mConnState+" new:"+newState+"]");
                    mConnState = newState;
                }
                if (!ConnectionState.CONNECTED.equals(newState)){
                    String popMsg = getResources().getText(R.string.notify_disconnected).toString();
                    Toast.makeText(MainActivity.this,popMsg+"[old:"+mConnState+" new:"+newState+"]",Toast.LENGTH_SHORT).show();
                }
            }

        }
    };


    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     * <p/>
     * <p>Keep in mind that onResume is not the best indicator that your activity
     * is visible to the user; a system window such as the keyguard may be in
     * front.  Use {@link #onWindowFocusChanged} to know for certain that your
     * activity is visible to the user (for example, to resume a game).
     * <p/>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mResumed = false;
        unregisterReceiver(mConnStateReceiver);
        (((CarLocationApplication) getApplicationContext()).getService()).unRegisterNotificationListener(this.mListener);
    }

    @Override
    public void getUserService() {
        mListener = new LocalListener();
        mNativeService = ((CarLocationApplication) getApplicationContext()).getService();
        if (mUserService == null) {
            mUserService = new UserService(mNativeService, mListener);
        }
    }


    /**
     * Register notification listener after getService() is not null.
     * Otherwise register for another time after 500ms.
     */
    @Override
    public void regNotifictListenner() {

        if (mNativeService != null) {
            mNativeService.registerNotificationListener(mListener);
        } else {
            Message mesg = Message.obtain(mHandler, REGISTER_NOTIFICATION);
            mHandler.sendMessageDelayed(mesg, 500);
        }

    }

    class LocalListener implements ResponseListener, NotificationListener {

        /**
         * Unsolicited message notification.
         *
         * @param noti
         */
        @Override
        public void onNotify(Notification noti) {
            forward(noti);
        }

        @Override
        public void onResponse(Notification noti) {
            forward(noti);
        }


        private void forward(Notification notif) {
            if (mResumed) {
                if (null == notif) {
                    Log.e(LOG_TAG, "handleMessage(): Error: Neither response nor unsolicited msg received from server.");
                    return;
                }
                Message message = Message.obtain(mHandler, HANDLE_NOTIFICATION, notif);
                mHandler.sendMessage(message);
            } else {
                Log.e(LOG_TAG, "onResponse():MainActivity has been destroyed,no alive handler set to handle message!");
                //TODO Add more action to save pending msg and re-handle after activity restart.
            }
        }
    }

    private Handler mHandler = new Handler(){
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case REGISTER_NOTIFICATION:
                    regNotifictListenner();
                    break;
                case HANDLE_NOTIFICATION:
                    Notification noti = (Notification) msg.obj;

                    switch (noti.notiType.ordinal()) {
                        case NOTIFY_TYPE_REQUEST: {
                            break;
                        }
                        case NOTIFY_TYPE_RESPONSE: {
                            //Deal with all response notify
                            handleResponseMessage(msg);
                            break;
                        }
                        case NOTIFY_TYPE_UNSOLICITED: {
                            //Deal with all unsolicited notify
                            handleUnsolicitedMessage(msg);
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }

            return;

        }
    };

    @Override
    public void handleResponseMessage(Message msg) {
        Notification noti = (Notification) msg.obj;
        MessageType msgType = noti.message.getMessageType();
        switch (msgType.ordinal()) {
            case LOCATION_MESSAGE:
                //Current we don't send location with response callback. So nothing to do.
                break;
            case IM_MESSAGE:
                //Current we don't send IM Msg with response callback. So nothing to do.
                break;
            case TASK_MESSAGE:
                handleTaskAssignmentMsgRsp(noti);
                break;
            case GLIDE_MESSAGE:
                handleGlidePathMsgRsp(noti);
                break;
            case WARN_MESSAGE:
                handleWarnMsgRsp(noti);
                break;
            case STATUS_MESSAGE:
                //Current we don't send IM Msg with response callback. So nothing to do.
                break;
            default:
                break;
        }

    }

    public void handleTaskAssignmentMsgRsp(Notification noti){

    }

    public void handleGlidePathMsgRsp(Notification noti){

    }

    public void handleWarnMsgRsp(Notification noti){

    }



    @Override
    public void handleUnsolicitedMessage(Message msg) {
        Notification noti = (Notification) msg.obj;
        MessageType msgType = noti.message.getMessageType();
        switch (msgType.ordinal()){
            case LOCATION_MESSAGE:
                handleLocations(noti);
                break;
            case IM_MESSAGE:
                handleImMsg(noti);
                break;
            case TASK_MESSAGE:
                handleTaskMsg(noti);
                break;
            case GLIDE_MESSAGE:
                handleGlideMsg(noti);
                break;
            case WARN_MESSAGE:
                handleWarnMsg(noti);
                break;
            case STATUS_MESSAGE:
                handleStatusMsg(noti);
                break;
            default:break;
        }

    }

    public void handleLocations(Notification noti){

    }
    public void handleImMsg(Notification noti){
        IMMessage imMsg = (IMMessage)noti.message;
        if (imMsg.mImMsgType == IMMessage.IMMsgType.IM_TXT_MSG){
            //Temporary to display toast content to user.
            IMTxtMessage txtMsg = (IMTxtMessage)imMsg;
            Toast.makeText(MainActivity.this,txtMsg.mTxtCont,Toast.LENGTH_SHORT).show();

        }else{
            //TODO Play voice to the user
        }
    }
    public void handleTaskMsg(Notification noti){

    }
    public void handleGlideMsg(Notification noti){

    }
    public void handleWarnMsg(Notification noti){

    }
    public void handleStatusMsg(Notification noti){

    }
}
