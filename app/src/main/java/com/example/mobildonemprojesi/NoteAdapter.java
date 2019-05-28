package com.example.mobildonemprojesi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;


public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {

    ArrayList<Note> note_list;
    LayoutInflater inflater;
    public NoteAdapter(Context context, ArrayList<Note> notes) {
        inflater = LayoutInflater.from(context);
        this.note_list = notes;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_row_layout, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ShowNoteActivity.class);
                i.putExtra("id", Integer.parseInt(((TextView)v.findViewById(R.id.note_id)).getText().toString()));
                v.getContext().startActivity(i);
            }
        });
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Note selectedProduct = note_list.get(position);
        holder.setData(selectedProduct, position);

    }

    @Override
    public int getItemCount() {
        return note_list.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView header, address , date, id_;
        ImageButton btn, btn2, ind;
        ConstraintLayout cl;

        public MyViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header_tv);
            date = itemView.findViewById(R.id.date_tv);
            btn = itemView.findViewById(R.id.edit_note_bt);
            btn2 = itemView.findViewById(R.id.delete_note_bt);
            cl = itemView.findViewById(R.id.card_cl);
            id_ = itemView.findViewById(R.id.note_id);
            address = itemView.findViewById(R.id.address_tw);
            ind = itemView.findViewById(R.id.indicator);
        }

        public void setData(final Note nt, int position) {
            this.header.setText(nt.getHeader());
            String s[] = nt.getTime().split("-");
            String s1[] = s[2].split(" ");
            this.date.setText(s1[0] + "/" +s[1] +"/"+s[0].substring(2) +" "+ s1[1]);

            String str[] = nt.getAdr().split("//");
            String s2[] = str[0].split(" ");

            String adr = s2[s2.length-2];
            adr = adr.substring(0, adr.length()-1);
            this.address.setText(adr);

            GradientDrawable gd = (GradientDrawable) ind.getBackground();
            gd.setColor(nt.getColor());
            if(nt.getColor() == Color.WHITE)
                gd.setColor(Color.LTGRAY);

            this.id_.setText(String.valueOf(nt.getId()));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), AddNoteActivity.class);
                    i.putExtra("id", nt.getId());
                    v.getContext().startActivity(i);
                }
            });
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Context ctx = v.getContext();
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    DBHelper db  = new DBHelper(ctx);
                                    db.deleteNote(nt.getId());
                                    ctx.startActivity(new Intent(ctx, MainActivity.class));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setIcon(android.R.drawable.ic_menu_delete).setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });
        }

        @Override
        public void onClick(View v) {

        }

    }
}