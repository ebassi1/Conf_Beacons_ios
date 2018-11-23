package com.conf_beacons_ios;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;
    String beacon;
    String beacon2;
    int ingresor = 0;
    int bluethooth = 0;
    public List<Beacon> beaconlijst;
    private TareaWSConsulta guardarbeacons = null;
    ProgressDialog dialog;
    Beacon PrimerBeacons;
    String res = null;
    Context context;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private ArrayList<BeaconInfo> beaconsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBluetooth(true);

        context = MainActivity.this;
        beaconsInfo = new ArrayList<BeaconInfo>();

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getInstanceForApplication(this).getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        //estado=(TextView) findViewById(R.id.estado_beacon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check?
            mPermissionCheck();
        }

    }


    //RANGING
    @Override
    public void onBeaconServiceConnect() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.isEnabled();
        }

        beaconManager.addRangeNotifier(new RangeNotifier() {
            // el servicio del beacon tarda frenar  y abre varias pantallas sin beaconEncontrados .

            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                beaconlijst = new ArrayList<>(beacons);

                if (beaconlijst.size() > 0) {
                    Log.e("distancia de beacons:", String.valueOf(String.valueOf(beacons.iterator().next().getBluetoothName())+":"+beacons.iterator().next().getDistance()));

                    if (beacons.iterator().next().getDistance() <= 3.1) {
                        if (ingresor == 0) {
                             PrimerBeacons = beaconlijst.get(0);

                            dispararWs(String.valueOf(beacons.iterator().next().getBluetoothName()));


                        }
                    }
                }


            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));


        } catch (RemoteException e) {
            Log.e("Error", e.toString());
        }

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                String a = region.toString();
            }

            @Override
            public void didExitRegion(Region region) {

                String a = region.toString();

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                String a = region.toString();

            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }


    public void dispararWs(String bluetoothName) {
        ingresor = 1;
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Configuracion:")
                .setMessage("Se va a configurar el:" + bluetoothName)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ingresor = 0;
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do something like...
                        guardarbeacons = new TareaWSConsulta();
                        guardarbeacons.execute();
                        dialog.cancel();
                    }
                }).show();


    }

    public boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (isEnabled == false) {
            bluethooth = 1;
        }
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    // Android M Permission check?
    @TargetApi(23)
    public void mPermissionCheck() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                //                @Override?
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }


    @Override
    @TargetApi(17)
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }


    @Override
    protected void onDestroy() {
        beaconManager.unbind(this);

        super.onDestroy();
    }

    private class TareaWSConsulta extends AsyncTask<String, Integer, Boolean> {

        int iniciar = 0;
        ProgressDialog progress;


        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Por favor espere...",
                    "Enviando Info", true);
            super.onPreExecute();
        }

        protected Boolean doInBackground(String... params) {

            final String NAMESPACE = "http://swerecaudador.smartmovepro.net/";
            final String URL = "https://swerecaudadordeterceros.smartmovepro.net/ModuloEntidades/SWEntidades.asmx";
            final String METHOD_NAME = "InsertarActualizarIbeaconPorNroSerie";
            final String SOAP_ACTION = "http://swerecaudador.smartmovepro.net/InsertarActualizarIbeaconPorNroSerie";


            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("usuario", "EMR");
            request.addProperty("clave", "WEBEMR274");
            request.addProperty("codigoEnteRecaudador", "274");
            request.addProperty("numeroSerieDispositivo", String.valueOf(PrimerBeacons.getBluetoothAddress()));
            request.addProperty("codigoTipoDispositivo", "1");
            request.addProperty("iBeaconUUID", String.valueOf(PrimerBeacons.getBeaconTypeCode()));
            request.addProperty("iBeaconMayor", "10001");
            request.addProperty("iBeaconMenor", "19641");

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE transporte = new HttpTransportSE(URL, 60000);
            try {
                transporte.call(SOAP_ACTION, envelope);
                SoapPrimitive resultado_xml = (SoapPrimitive) envelope.getResponse();
                String a = "";
                res = resultado_xml.toString();
            } catch (Exception e) {
                String a = e.toString();
            }


            return null;

        }

        protected void onPostExecute(Boolean result) {
            progress.dismiss();

            if (res.equals("0|ok")) {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Configurado!")
                        .setMessage("desea Configurar otro dispositivo")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                                finish();
                            }
                        })
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // do something like...
                                ingresor = 0;
                                PrimerBeacons = null;


                            }
                        }).show();



            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        res,
                        Toast.LENGTH_SHORT);

                toast.show();
                ingresor = 0;
                PrimerBeacons = null;


            }

        }
    }


}
