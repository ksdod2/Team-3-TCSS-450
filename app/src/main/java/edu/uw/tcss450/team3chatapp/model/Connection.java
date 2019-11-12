package edu.uw.tcss450.team3chatapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Class to represent a user information related to a connection.
 * @author Kameron Dodd
 * @version 11/7/19
 */
public class Connection implements Serializable, Parcelable {

    // Enum for the potential relations of a connection: Accepted, Pending, or no relation
    public enum Relation {
        ACCEPTED, UNACCEPTED, NONE
    }

    private int mMemberID;
    private final String mFirstName;
    private final String mLastName;
    private final String mUsername;
    private final String mEmail;
    private Relation mIsAccepted;
    private boolean mAmSender;

    public Connection(final int memberID, final String fName, final String lName, final String username,
                      final String email, final Integer relation, final boolean sender) {
        mMemberID = memberID;
        mFirstName = fName;
        mLastName = lName;
        mUsername = username;
        mEmail = email;
        mAmSender = sender;
        // Determine relation by integer or null returned in row
        if (relation == -1)
            mIsAccepted = Relation.NONE;
        else
            mIsAccepted = (relation.equals(1)) ? Relation.ACCEPTED : Relation.UNACCEPTED;
    }

    protected Connection(Parcel in) {
        mFirstName = in.readString();
        mLastName = in.readString();
        mUsername = in.readString();
        mEmail = in.readString();
        mAmSender = in.readByte() != 0;
    }

    public int getMemberID() { return mMemberID; }
    public String getFirstName() { return mFirstName; }
    public String getLastName() { return mLastName; }
    public String getUsername() { return mUsername; }
    public String getEmail() { return mEmail; }
    public Relation getIsAccepted() { return mIsAccepted; }
    public boolean amSender() { return mAmSender; }

    public static final Creator<Connection> CREATOR = new Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel in) {
            return new Connection(in);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mUsername);
        dest.writeString(mEmail);
    }

}
