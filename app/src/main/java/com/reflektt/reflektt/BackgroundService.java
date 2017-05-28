package com.reflektt.reflektt;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.reflektt.reflektt.Tables.Posts;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BackgroundService extends IntentService {
    private BackendlessUser currentUser;
    private static BackgroundService bService = null;
    private Bitmap profilePicture;

    public BackgroundService() {
        super("BackgroundService");
    }

    public static BackgroundService getService() {
        if (bService == null)
            bService = new BackgroundService();
        return bService;

    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String convertBitmapToString(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String imageStr;
        bmp.compress(Bitmap.CompressFormat.PNG, 75, stream); //compress to which format you want.
        byte[] byte_arr = stream.toByteArray();
        imageStr = Base64.encodeToString(byte_arr, Base64.NO_WRAP | Base64.NO_PADDING);
        return imageStr;
    }

    public Bitmap stringToBitmap(String string) {
        byte[] encodeByte = Base64.decode(string, Base64.NO_WRAP | Base64.NO_PADDING);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        return bitmap;
    }

    public Bitmap bitmapScalar(Bitmap bitmap, String selectedImage) {
        int MAX_DIM = 600;
        if ((bitmap.getWidth() >= MAX_DIM) && (bitmap.getHeight() >= MAX_DIM)) {
            BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
            bmpOptions.inSampleSize = 1;
            while ((bitmap.getWidth() >= MAX_DIM) && (bitmap.getHeight() >= MAX_DIM)) {
                bmpOptions.inSampleSize *= 2;
                bitmap = BitmapFactory.decodeFile(selectedImage, bmpOptions);
            }
        }

        return stringToBitmap(convertBitmapToString(bitmap));
    }

    public String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public BackendlessUser getUser() {
        return currentUser;
    }

    public void setCurrentUser(BackendlessUser user) {
        currentUser = user;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            //if action is follow or unfollow
            if (action.equals(Constants.ACTION_UPLOAD)) {
                String uploadFilePath = intent.getStringExtra("path");
                HttpURLConnection conn;
                DataOutputStream dos;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1024 * 1024;
                File sourceFile = new File(uploadFilePath);

                if (sourceFile.isFile()) {
                    try {

                        // open a URL connection to the Servlet
                        FileInputStream fileInputStream = new FileInputStream(sourceFile);
                        URL url = new URL("http://reflektt.16mb.com/upload.php");

                        // Open a HTTP  connection to  the URL
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true); // Allow Inputs
                        conn.setDoOutput(true); // Allow Outputs
                        conn.setUseCaches(false); // Don't use a Cached Copy
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", uploadFilePath);

                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                                + uploadFilePath + "\"" + lineEnd);

                        dos.writeBytes(lineEnd);

                        // create a buffer of  maximum size
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        // read file and write it into form...
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        }

                        // send multipart form data necesssary after file data...
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        //Response from the server
                        InputStream is = new BufferedInputStream(conn.getInputStream());
                        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
                        String string = s.hasNext() ? s.next() : "";
                        if(string.equals("fail"))
                            Toast.makeText(getApplicationContext(),R.string.error,Toast.LENGTH_SHORT).show();


                        //close the streams //
                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        // Log.e("Upload Exception", "Exception : " + e.getMessage(), e);

                    } // End else block
                }
            }

            //// TODO : adding new intent handling
        }
    }

    private boolean updateUsers(BackendlessUser user, BackendlessUser followed) {
        try {
            BackendlessUser current = Backendless.UserService.update(user);
            getService().setCurrentUser(current);
            Backendless.UserService.update(followed);
            return true;
        } catch (BackendlessException e) {
            return false;
        }
    }

    public boolean follow(String followed) {
        BackendlessDataQuery query = new BackendlessDataQuery();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.addRelated("followings");
        queryOptions.addRelated("followers");
        queryOptions.addRelated("posts");
        queryOptions.addRelated("posts.comments.commentedUser");
        queryOptions.addRelated("favorite_items");
        query.setQueryOptions(queryOptions);
        query.setWhereClause(String.format("objectId='%s'", followed));
        try {
            BackendlessUser followedUser = Backendless.Data.of(BackendlessUser.class).find(query).getData().get(0);
            //making new data to contain new menus
            Object[] userFollowing = (Object[]) getService().getUser().getProperty("followings");
            Object[] userFollowers = (Object[]) followedUser.getProperty("followers");
            BackendlessUser[] newFollowing;
            BackendlessUser[] newFollowers;

            //if following menu isn't empty, copy all data inside the new container,then add the followed person
            if (userFollowing.length > 0) {
                newFollowing = new BackendlessUser[userFollowing.length + 1];
                BackendlessUser[] following = (BackendlessUser[]) userFollowing;
                System.arraycopy(following, 0, newFollowing, 0, userFollowing.length);
                newFollowing[userFollowing.length] = followedUser;
            }
            // if empty, the menu will include just the followed person
            else {
                newFollowing = new BackendlessUser[]{followedUser};
            }
            //if follower menu isn't empty, copy all data inside the new container,then add the current user
            if (userFollowers.length > 0) {
                newFollowers = new BackendlessUser[userFollowers.length + 1];
                BackendlessUser[] followers = (BackendlessUser[]) userFollowers;
                System.arraycopy(followers, 0, newFollowers, 0, userFollowers.length);
                newFollowers[userFollowers.length] = getService().getUser();
            }
            // if empty, the menu will include just the current user
            else {
                newFollowers = new BackendlessUser[]{getService().getUser()};
            }
            getService().getUser().setProperty("followings", newFollowing);
            followedUser.setProperty("followers", newFollowers);
            return updateUsers(getService().getUser(), followedUser);
        } catch (BackendlessException e) {
            return false;
        }

    }

    public boolean unfollow(String followed) {
        BackendlessDataQuery query = new BackendlessDataQuery();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.addRelated("followings");
        queryOptions.addRelated("followers");
        queryOptions.addRelated("posts");
        queryOptions.addRelated("posts.comments.commentedUser");
        queryOptions.addRelated("favorite_items");
        query.setQueryOptions(queryOptions);
        query.setWhereClause(String.format("objectId='%s'", followed));
        try {
            BackendlessUser followedUser = Backendless.Data.of(BackendlessUser.class).find(query).getData().get(0);
            //making new data to contain new followers and following menus
            Object[] userFollowing = (Object[]) getService().getUser().getProperty("followings");
            Object[] userFollowers = (Object[]) followedUser.getProperty("followers");
            BackendlessUser[] newFollowing;
            BackendlessUser[] newFollowers;

            //if following menu is greater than 1, copy items and skip the followed user
            if (userFollowing.length > 1) {
                BackendlessUser[] following = (BackendlessUser[]) userFollowing;
                newFollowing = new BackendlessUser[userFollowing.length - 1];
                int j = 0;
                for (BackendlessUser aFollowing : following) {
                    if (aFollowing.getObjectId().equals(followedUser.getObjectId())) continue;
                    newFollowing[j++] = aFollowing;
                }
            }
            //if equals 1, delete menu
            else
                newFollowing = new BackendlessUser[0];

            //if follower menu is greater than 1, copy items and skip the current user
            if (userFollowers.length > 1) {
                newFollowers = new BackendlessUser[userFollowers.length - 1];
                BackendlessUser[] followers = (BackendlessUser[]) userFollowers;
                int j = 0;
                for (BackendlessUser follower : followers) {
                    if (follower.getObjectId().equals(getService().getUser().getObjectId()))
                        continue;
                    newFollowers[j++] = follower;
                }
            }
            //if equals 1, delete menu
            else
                newFollowers = new BackendlessUser[0];

            //save data in user objects
            getService().getUser().setProperty("followings", newFollowing);
            followedUser.setProperty("followers", newFollowers);
            return updateUsers(getService().getUser(), followedUser);
        } catch (BackendlessException e) {
            return false;
        }
    }

    public void add_post(Posts post){
        Posts[] newPosts;
        Object[] postObjects = (Object[]) getService().getUser().getProperty("posts");
        if (postObjects!=null && postObjects.length != 0){
            Posts[] oldPosts = (Posts[])postObjects;
            newPosts = new Posts[postObjects.length+1];
            newPosts[0] = post;
            System.arraycopy(oldPosts,0,newPosts,1,oldPosts.length);
        }
        else{
            newPosts = new Posts[1];
            newPosts[0]=post;
        }
        getService().getUser().setProperty("posts",newPosts);
    }

}
