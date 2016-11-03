package test.shalu.com.MyEddystone;

/**
 * Created by user on 14/09/2016.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter{
    ArrayList <String>distance;
    ArrayList <String>rssi;
    ArrayList <String> url;
    ArrayList <String> devname;

    String clickedUrl="";
    Beacon context;
    int [] imageId;
    private static LayoutInflater inflater=null;
    public CustomAdapter(Beacon mainActivity,ArrayList<String> Device, ArrayList<String> Url,ArrayList<String> RSSI,ArrayList<String> Distance) {
        // TODO Auto-generated constructor stub

        context=mainActivity;
        //imageId=prgmImages;

        url=Url;
        distance=Distance;
        devname=Device;
        rssi=RSSI;

        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
   @Override
    public int getCount() {
        // TODO Auto-generated method stub
       return url.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView DEVname,rs,url,dis,txpower;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.program_list, null);
        holder.DEVname=(TextView) rowView.findViewById(R.id.textView1);
        holder.rs=(TextView)rowView.findViewById(R.id.textView2);
        holder.url=(TextView)rowView.findViewById(R.id.textView3);
        holder.dis=(TextView)rowView.findViewById(R.id.textView4);
        holder.img=(ImageView) rowView.findViewById(R.id.imageView1);

        holder.DEVname.setText(devname.get(position).toString());
        holder.url.setText(url.get(position).toString());
        holder.dis.setText(distance.get(position).toString());
        holder.rs.setText(rssi.get(position).toString());
        // holder.img.setImageResource(imageId[position]);

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
               // Toast.makeText(context, "You Clicked "+url.get(position).toString(), Toast.LENGTH_LONG).show();
                clickedUrl=url.get(position).toString();
                Beacon.adapterRespond(clickedUrl);

               /* byte[] data =scanRec.get(position);
                int rssivalue=Integer.parseInt(rssi.get(position));
                 Log.d("byteval", rssivalue + " " + data);
                context.connect(rssivalue,data);*/

            }
        });
        return rowView;
    }


}