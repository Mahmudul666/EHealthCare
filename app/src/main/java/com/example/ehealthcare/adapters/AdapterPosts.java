package com.example.ehealthcare.adapters;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ehealthcare.AddPostActivity;
import com.example.ehealthcare.PostDetailActivity;
import com.example.ehealthcare.R;
import com.example.ehealthcare.ThereProfileActivity;
import com.example.ehealthcare.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

  public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{
    Context context;
    List<ModelPost> postList;

    String myUid;

      private DatabaseReference postsRef;
    private DatabaseReference likeRef;


    boolean mProcessLike=false;
    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

      @NonNull
      @Override
      public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts,viewGroup,false);

          return new MyHolder(view);
      }

      @Override
      public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        final String uid = postList.get(i).getUid();
          String uEmail = postList.get(i).getuEmail();
          String uName = postList.get(i).getuName();
          String uDp = postList.get(i).getuDp();
          final String pId = postList.get(i).getpId();//
          String pTitle = postList.get(i).getpTitle();
          String pDescription = postList.get(i).getpDescr();
          final String pImage = postList.get(i).getpImage();
          String pTimeStamp = postList.get(i).getpTime();
          String pLikes = postList.get(i).getpLikes();
          String pComments = postList.get(i).getpComments();

          Calendar calendar = Calendar.getInstance(Locale.getDefault());
          calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
          String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

          myHolder.uNameTv.setText(uName);
          myHolder.pTimeTv.setText(pTime);
          myHolder.pTitleTv.setText(pTitle);
          myHolder.pDescriptionTv.setText(pDescription);
          myHolder.pLikesTv.setText(pLikes + "Likes");
          myHolder.pCommentsTv.setText(pComments + "Comments");
          setLikes(myHolder,pId);

            if(pImage.equals("noImage")){
                myHolder.pImageIv.setVisibility(View.GONE);

            }else {
                myHolder.pImageIv.setVisibility(View.VISIBLE);
                try{
                    Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(myHolder.uPictureIv);

                }catch (Exception e){

                }
            }



          try{
              Picasso.get().load(pImage).into(myHolder.pImageIv);

          }catch (Exception e){

          }

          myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
              //@RequiresApi(api = Build.VERSION_CODES.KITKAT)
              @Override
              public void onClick(View v) {
                  showMoreOptions(myHolder.moreBtn,uid,myUid,pId,pImage);
              }
          });
          myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  final int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                  mProcessLike = true;
                  final String postIde = postList.get(i).getpId();
                  likeRef.addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if(mProcessLike){
                              if(dataSnapshot.child(postIde).hasChild(myUid)){
                                  postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                  likeRef.child(postIde).child(myUid).removeValue();
                                  mProcessLike=false;
                              }else{
                                  postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                  likeRef.child(postIde).child(myUid).setValue("Liked");
                                  mProcessLike = false;
                              }
                          }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });

              }
          });
          myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(context, PostDetailActivity.class);
                  intent.putExtra("postId",pId);
                  context.startActivity(intent);
              }
          });
          myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Toast.makeText(context,"share",Toast.LENGTH_SHORT).show();
              }
          });

          myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(context, ThereProfileActivity.class);
                  intent.putExtra("uid",uid);
                  context.startActivity(intent);
              }
          });

      }

      private void setLikes(final MyHolder holder, final String postKey) {

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postKey).hasChild(myUid)){
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    holder.likeBtn.setText("Liked");
                }else{
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


      }


      @TargetApi(Build.VERSION_CODES.KITKAT)
      private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {

          //PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);
          PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

          if(uid.equals(myUid)){
              popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
              popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
          }
          popupMenu.getMenu().add(Menu.NONE,2,0,"View Detail");

          popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
              @Override
              public boolean onMenuItemClick(MenuItem menuitem) {

                  int id = menuitem.getItemId();
                  if(id==0){
                      beginDelete(pId,pImage);
                  }else if(id==1){
                      Intent intent = new Intent(context, AddPostActivity.class);
                      intent.putExtra("key","editPost");
                      intent.putExtra("editPostId",pId);
                      context.startActivity(intent);

                  } else if (id==2){
                      Intent intent = new Intent(context, PostDetailActivity.class);
                      intent.putExtra("postId",pId);
                      context.startActivity(intent);
                  }

                  return false;
              }
          });
          popupMenu.show();



      }

      private void beginDelete(String pId, String pImage) {
        
        if(pImage.equals("noImage")){
            deleteWithoutImage(pId);
        }else{
            deleteWithImage(pId,pImage);
        }

        
      }

      private void deleteWithImage(final String pId, String pImage) {

          final ProgressDialog pd = new ProgressDialog(context);
          pd.setMessage("Deleting...");
          StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
          picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {

                  Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                  fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          for(DataSnapshot ds: dataSnapshot.getChildren()){
                              ds.getRef().removeValue();
                          }

                          Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
                          pd.dismiss();


                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });

              }
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {

                  pd.dismiss();
                  Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

              }
          });
      }

      private void deleteWithoutImage(String pId) {
          final ProgressDialog pd = new ProgressDialog(context);
          pd.setMessage("Deleting...");
          Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
          fquery.addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  for(DataSnapshot ds: dataSnapshot.getChildren()){
                      ds.getRef().removeValue();
                  }

                  Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
                  pd.dismiss();


              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });
      }


      @Override
      public int getItemCount() {
          return postList.size();
      }

      class MyHolder extends RecyclerView.ViewHolder {

    ImageView uPictureIv,pImageIv;
    TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikesTv,pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn,commentBtn,shareBtn;
    LinearLayout profileLayout;
    public MyHolder (@NonNull View itemView) {
        super(itemView);
        uPictureIv = itemView.findViewById(R.id.uPictureIv);
        pImageIv = itemView.findViewById(R.id.pImageIv);
        uNameTv = itemView.findViewById(R.id.uNameTv);
        pTimeTv = itemView.findViewById(R.id.pTimeTv);
        pTitleTv = itemView.findViewById(R.id.pTitleTv);
        pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
        pLikesTv = itemView.findViewById(R.id.pLikesTv);
        pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
        moreBtn = itemView.findViewById(R.id.moreBtn);
        likeBtn = itemView.findViewById(R.id.likeBtn);
        commentBtn = itemView.findViewById(R.id.commentBtn);
        shareBtn = itemView.findViewById(R.id.shareBtn);
        profileLayout = itemView.findViewById(R.id.profileLayout);
    }
  }
}
