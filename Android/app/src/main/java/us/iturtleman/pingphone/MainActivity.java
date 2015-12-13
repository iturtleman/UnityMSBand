package us.iturtleman.pingphone;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.tiles.BandTile;
import com.microsoft.band.tiles.TileButtonEvent;
import com.microsoft.band.tiles.TileEvent;
import com.microsoft.band.tiles.pages.FlowPanel;
import com.microsoft.band.tiles.pages.FlowPanelOrientation;
import com.microsoft.band.tiles.pages.PageData;
import com.microsoft.band.tiles.pages.PageLayout;
import com.microsoft.band.tiles.pages.FilledButtonData;
import com.microsoft.band.tiles.pages.TextButtonData;
import com.microsoft.band.tiles.pages.FilledButton;
import com.microsoft.band.tiles.pages.TextButton;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity {

    private BandClient client = null;
    private Button btnStart;
    private TextView txtStatus;

    private static final UUID tileId = UUID.fromString("cc0D508F-70A3-47D4-BBA3-812BADB1F8Aa");
    private static final UUID pageId1 = UUID.fromString("b1234567-89ab-cdef-0123-456789abcd00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //txtStatus.setText("");
                new appTask().execute();
            }
        });
        //*/
        IntentFilter filter = new IntentFilter();
        filter.addAction(TileEvent.ACTION_TILE_OPENED);
        filter.addAction(TileEvent.ACTION_TILE_BUTTON_PRESSED);
        filter.addAction(TileEvent.ACTION_TILE_CLOSED);
        registerReceiver(messageReceiver, filter);

    }

    public void CreateBandTile(){
        new CreateBand().execute(true);
    }

    public void RemoveBandTiles(){
        new CreateBand().execute(false);
    }

    public void VolumeUp()
    {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int volume = Math.min(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)+10, audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, volume, 0);
    }

    public void VolumeDown()
    {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int volume = Math.max(audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) - 10, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,volume,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        unregisterReceiver(messageReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == TileEvent.ACTION_TILE_OPENED) {
                TileEvent tileOpenData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);
                appendToUI("Tile open event received\n" + tileOpenData.toString()+ "\n\n");
            } else if (intent.getAction() == TileEvent.ACTION_TILE_BUTTON_PRESSED) {
                TileButtonEvent buttonData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);
                appendToUI("Button event received\n" + buttonData.toString()+ "\n\n");
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction() == TileEvent.ACTION_TILE_CLOSED) {
                TileEvent tileCloseData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);
                appendToUI("Tile close event received\n" + tileCloseData.toString()+ "\n\n");
            }
        }
    };

    public class CreateBand extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... params) {
            try {
                if(params.length>0) {
                    boolean shouldAdd = params[0];
                    if (getConnectedBandClient()) {
                        appendToUI("Band is connected.\n");
                        if (shouldAdd) {
                            if (addTile()) {
                                updatePages();
                            } else
                                appendToUI("Failed to add page");
                        } else {
                            removeTile();
                        }
                    } else {
                        appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                    }
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case DEVICE_ERROR:
                        exceptionMessage = "Please make sure bluetooth is on and the band is in range.\n";
                        break;
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    case BAND_FULL_ERROR:
                        exceptionMessage = "Band is full. Please use Microsoft Health to remove a tile.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

    public void appendToUI(final String string) {
        UnityPlayer.UnitySendMessage("MessageHandler","HandleText",string);
    }

    public boolean doesTileExist(List<BandTile> tiles, UUID tileId) {
        for (BandTile tile:tiles) {
            if (tile.getTileId().equals(tileId)) {
                return true;
            }
        }
        return false;
    }

    public boolean addTile() throws Exception {
        if (doesTileExist(client.getTileManager().getTiles().await(), tileId)) {
            return true;
        }

		/* Set the options */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap tileIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.raw.tile_icon_large, options);

        BandTile tile = new BandTile.Builder(tileId, "Button Tile", tileIcon)
                .setPageLayouts(createButtonLayout())
                .build();
        appendToUI("Button Tile is adding ...\n");
        if (client.getTileManager().addTile(this, tile).await()) {
            appendToUI("Button Tile is added.\n");
            return true;
        } else {
            appendToUI("Unable to add button tile to the band.\n");
            return false;
        }
    }

    public void removeTile() throws Exception {
        appendToUI("Trying to Remove tile from the band\n");
        client.getTileManager().removeTile(tileId);
    }

    public PageLayout createButtonLayout() {
        return new PageLayout(
                new FlowPanel(15, 0, 260, 105, FlowPanelOrientation.VERTICAL)
                        .addElements(new TextButton(0, 5, 210, 45).setMargins(0, 5, 0 ,0).setId(1).setPressedColor(Color.BLUE))
                        .addElements(new TextButton(0, 0, 210, 45).setMargins(0, 5, 0 ,0).setId(2).setPressedColor(Color.GREEN))
        );
    }

    public void updatePages() throws BandIOException {
        client.getTileManager().setPages(tileId,
                new PageData(pageId1, 0)
                        .update(new TextButtonData(1, "Volume Up"))
                        .update(new TextButtonData(2, "Volume Down")));
        appendToUI("Send button page data to tile page \n\n");
    }

    public boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }


/*
    private BandClient client = null;
    private Button btnStart;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtStatus.setText("");
                new appTask().execute();
            }
        });
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == TileEvent.ACTION_TILE_OPENED) {
                TileEvent tileOpenData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);
                appendToUI("Tile open event received\n" + tileOpenData.toString() + "\n\n");
            } else if (intent.getAction() == TileEvent.ACTION_TILE_BUTTON_PRESSED) {
                TileButtonEvent buttonData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);
                appendToUI("Button event received\n" + buttonData.toString() + "\n\n");
            } else if (intent.getAction() == TileEvent.ACTION_TILE_CLOSED) {
                TileEvent tileCloseData = intent.getParcelableExtra(TileEvent.TILE_EVENT_DATA);
                appendToUI("Tile close event received\n" + tileCloseData.toString() + "\n\n");
            }
        }
    };

    private PageLayout createButtonLayout() {
        return new PageLayout(
                new FlowPanel(15, 0, 260, 105, FlowPanelOrientation.VERTICAL)
                        .addElements(new FilledButton(0, 5, 210, 45).setMargins(0, 5, 0, 0).setId(12).setBackgroundColor(Color.RED))
                        .addElements(new TextButton(0, 0, 210, 45).setMargins(0, 5, 0, 0).setId(21).setPressedColor(Color.BLUE))
        );
    }


    private boolean addTile(BandClient c){
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap tileIcon = BitmapFactory.decodeResource(getBaseContext().getResources(), R.raw.tile_icon_large, options);
            return MSBandWrapper.addTile(c, this, tileIcon, createButtonLayout());
        }
        catch (Exception e){
            appendToUI(e.toString());
            return false;
        }
    }

    private class appTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
                appendToUI(new Integer(devices.length).toString());
                if (devices.length == 0)
                {
                    appendToUI("Band isn't paired with your phone.");
                }
                client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);

                //Pair<BandClient,String> connectResult = MSBandWrapper.getConnectedBandClient(client==null?null:client,getBaseContext());
                //client = connectResult.first;
                //appendToUI(connectResult.second);

                if (client!=null) {
                    appendToUI("Band is connected.\n");
                    if (addTile(client)){

                        MSBandWrapper.updatePages(client);
                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case DEVICE_ERROR:
                        exceptionMessage = "Please make sure bluetooth is on and the band is in range.\n";
                        break;
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    case BAND_FULL_ERROR:
                        exceptionMessage = "Band is full. Please use Microsoft Health to remove a tile.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        MSBandWrapper.registerMessageReceiver(getBaseContext(),messageReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MSBandWrapper.unregisterMessageReceiver(getBaseContext(),messageReceiver);
    }

    @Override
    protected void onDestroy() {
        MSBandWrapper.destroyClient(client);
        super.onDestroy();
    }

    void appendToUI(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.append(string);
            }
        });
    }
//*/
}
