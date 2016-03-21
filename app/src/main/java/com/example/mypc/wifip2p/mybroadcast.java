package com.example.mypc.wifip2p;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
/**
 * Created by My Pc on 20-02-2016.
 */
public class mybroadcast extends BroadcastReceiver   {
    Intent i=new Intent();
    int dflag=0,pflag=1,flag=0;
    String data;
    private WifiP2pManager mManager;
    NetworkInfo networkInfo;
    WifiP2pDevice thisdevice;
    WifiP2pInfo wifiinfo;
    private WifiP2pManager.Channel mChannel;
    WifiP2pConfig config;
    Context c;
    ArrayList<WifiP2pDevice> peerlist=new ArrayList<WifiP2pDevice>();
    public mybroadcast(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
    }
    @Override
    public void onReceive(Context context, final Intent intent) {
        String action = intent.getAction();
        c=context;
        i = intent;
            if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

                thisdevice = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                Toast.makeText(context, "This device--"+thisdevice.deviceName, Toast.LENGTH_LONG).show();
                Log.e("this device address", thisdevice.deviceAddress);
                Log.e("this device name", thisdevice.deviceName);

            } else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

                Log.e("inside", "WIFI_P2P_STATE_CHANGED_ACTION");
                int x = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (x == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                    Log.e("state", "enabled");
                else
                    Log.e("state", "disabled");

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                Log.e("peer", " before WIFI_P2P_PEERS_CHANGED_ACTION  "+pflag);
                if (pflag==0)
                {
                    pflag=1;
                    Log.e("peer", " after WIFI_P2P_PEERS_CHANGED_ACTION");
                    mManager.requestPeers((WifiP2pManager.Channel) mChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        peerlist.clear();
                        peerlist.addAll(peers.getDeviceList());
                        if (peerlist.size() == 0)
                            Log.e("on peer available", "no devices available");
                        else {
                            flag = 1;
                            Log.e("loop", peerlist.size()+" ");
                            if (dflag == 1) {
                                    dflag = 0;
                                    config = new WifiP2pConfig();
                                    config.deviceAddress = peerlist.get(0).deviceAddress;
                                    config.groupOwnerIntent=15;
                                    config.wps.setup = WpsInfo.PBC;
                                    Log.e("device address:", peerlist.get(0).deviceAddress);
                                    String dev_name = peerlist.get(0).deviceName;
                                    Log.e("connect", "connecting to..." + dev_name);
                                    Toast.makeText(c, "connecting to..." + dev_name, Toast.LENGTH_LONG).show();
                                    connection();
                                    flag=1;
                            } else if (dflag == 0) {
                                    config = new WifiP2pConfig();
                                    config.deviceAddress = peerlist.get(peerlist.size()-1).deviceAddress;
                                    config.groupOwnerIntent=15;
                                    config.wps.setup = WpsInfo.PBC;
                                    if(!config.deviceAddress.equals(wifiinfo.groupOwnerAddress)) {
                                        Log.e("device address:", peerlist.get(peerlist.size()-1).deviceAddress);
                                        String dev_name = peerlist.get(peerlist.size()-1).deviceName;
                                        Log.e("connect", "connecting to..." + dev_name);
                                        Toast.makeText(c, "connecting to..." + dev_name, Toast.LENGTH_LONG).show();
                                        connection();
                                        flag = 1;
                                    }
                                    else
                                    {
                                        Log.e("this ","device is the sender");
                                    }
                                }
                            Log.e("loop", "ends");
                        }
                    }
                });
            }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Log.e("inside ", "WIFI_P2P_CONNECTION_CHANGED_ACTION");
                networkInfo = (NetworkInfo) i.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null)
                    Log.e("networkinfo ", networkInfo.toString());
                request();
            }
    }//onrec

    private class backc extends AsyncTask<Void ,Void,String>{
       @Override
        protected String  doInBackground(Void...x)
        {               flag=1;
                        Log.e("inside ","doinbackgrnd of client");
                    try {
                        Socket s= new Socket();
                        s.connect(new InetSocketAddress(wifiinfo.groupOwnerAddress,5264));
                        Log.e("client ", s.toString());
                        InputStream is = s.getInputStream();
                        ObjectInputStream ois = new ObjectInputStream(is);
                        data= (String)ois.readObject();
                        is.close();
                        s.close();
                        Log.e("data received in client",data);
                        if(thisdevice.deviceName.equals("iris 450P") )
                        {
                            Log.e("successfully completed", "thankuuuu");
                            return "1";
                        }
                        else
                        {
                            Log.e("forwarding", "ok");
                            disconnect();
                            Log.e("going to sleep", "k");
                            Thread.sleep(6000);
                            Log.e("after sleep", "k");
                            dflag=0;
                            pflag=0; //for peer to peer
                            discover();
                        }
                    }catch(Exception e){Log.e("exception",e.toString());}

        return "0";
        }
    @Override
    public void onPostExecute(String  m)
    {
        flag=0;
        if(m.equals("1"))
        {
            Toast.makeText(c, "destination reached!", Toast.LENGTH_LONG).show();
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(c)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setDefaults(Notification.DEFAULT_ALL);
            mBuilder.setContentTitle("Emergency!!!!");
            mBuilder.setContentText(data);
            Intent resultIntent = new Intent(c.getApplicationContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(c);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
            mNotificationManager.notify(11, mBuilder.build());


        }
        Log.e("doinbackground of client","client");
    }
}

    private class backs extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void  doInBackground(Void...x)
        {   flag=1;
            Log.e("doinbackgrnd of ","server");

                try{
                    ServerSocket ss = new ServerSocket(5264);
                    Socket s=ss.accept();
                    data="Your Friend  is in trouble...Hurry!!";
                    Log.e("server","server socket accepted"+s.toString());
                    OutputStream os = s.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    oos.writeObject(data);
                    Log.e("server", "oos=" + oos.toString());
                    oos.close();
                    os.close();
                    s.close();
                    Log.e("wifi", "disabling server");
                    WifiManager wmanager=(WifiManager) c.getSystemService(Context.WIFI_SERVICE);
                    wmanager.setWifiEnabled(false);
                    disconnect();
                }catch(Exception e){System.out.println(e);}
            return null;
        }
        @Override
        public void onPostExecute(Void m)
        {
            flag=0;
            Log.e(" onpostexecute of ", "server");
        }
    }

    public  void connection() {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("connection", "initialised");
                Toast.makeText(c, "connection initialised ", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(int reason) {
                Log.e("connection ", "failed");
            }
        });
    }
    public void  request()
    {
                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                        if (wifiP2pInfo.groupFormed) {
                            wifiinfo = wifiP2pInfo;
                            Log.e("wifi Group Owner address", wifiinfo.groupOwnerAddress.toString());
                            if (wifiP2pInfo.isGroupOwner) {
                                Log.e("group ", "owner");
                                new backs().execute();
                            } else {
                                Log.e("group", "client");
                                Toast.makeText(c, "client in the group", Toast.LENGTH_LONG).show();
                                new backc().execute();
                             }
                        }
                        else
                        {
                            Toast.makeText(c, "no group formed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        }
    public void discover()
    {
        mManager.discoverPeers((WifiP2pManager.Channel) mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.e("discover", "success");
                Toast.makeText(c, "discovered", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.e("discover", "failure");
            }
        });
    }
    public void disconnect()
    {   Log.e("before ","disconnect"+mManager.toString()+" "+mChannel.toString());
        if(mManager!=null &&mChannel!=null){
            Log.e("inside ","disconnect");
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {

                    mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Log.e("disconnection", "success");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.e("disconnection ", "failed");
                        }
                    });
                }
            });
        }
    }
}//class
