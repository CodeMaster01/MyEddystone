# MyEddystone

A simple app that demonstrate use of Proximity [Eddystone](https://kontakt.io/blog/what-is-eddystone/) beacons,which it uses to list Eddystone beacons on live devices.

# Requirements

Application was developed by using [Android studio] (https://developer.android.com/studio/index.html) and target Kitkat 4.4.2 and higher [Android OS Versions] (https://en.wikipedia.org/wiki/Android_version_history)

You must have a beacon,that [setup as an Eddystone](https://developers.google.com/beacons/get-started) 

#Technologies 

In this application use some open source libraries : 

RxJava- ReactiveX Java VM

RXAndroid- ReactiveX Android module

ButterKnife- Field and method binding for Android views

Android Bootstrap- custom views styled according to the Twitter Bootstrap

#Read Nearby BLE Devicess

check available BLE devices using runnable

      private BluetoothAdapter.LeScanCallback mLeScanCallback =new BluetoothAdapter.LeScanCallback() {
        @Override
        
        public void onLeScan(final BluetoothDevice device,final int rssi,final byte[] scanRecord)
        
        {
        
            // Parse the payload of the advertisement packet
            // as a list of AD structures.
                 new Thread()
                {
                    public void run()
                    {
                        Beacon.this.runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                connect(rssi,scanRecord,device);


                            }
                        });
                    }
                }.start();
            }

    };

# Connect to Device
read eddystones based on the scan result and connect to nearby Eddystone.

    public void connect(final int  rssi, byte[] scanRecord,final BluetoothDevice device) {

        Log.d("recei", rssi + " " + scanRecord);
       final List<ADStructure> structures =
                ADPayloadParser.getInstance().parse(scanRecord);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // For each AD structure contained in the advertisement packet.
                for (ADStructure structure : structures) {

                    //get only Eddystone from structure
                    if (structure instanceof EddystoneURL) {

                        // Eddystone URL
                        EddystoneURL es = (EddystoneURL) structure;

                        Log.d("Eddy", "Tx Power = " + es.getTxPower());
                        Log.d("Eddy", "URL = " + es.getURL() + " length : ");

                    } else {

                    }

                }

            }
        }, 4000);
       

    }
