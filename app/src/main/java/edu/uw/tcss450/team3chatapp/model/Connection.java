package edu.uw.tcss450.team3chatapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Class to represent a user information related to a connection.
 * @author Kameron Dodd
 * @version 11/18/19
 */
public class Connection implements Serializable, Parcelable {

    /** Enum for the potential relations of a connection: Accepted, Pending, or No relation. */
    public enum Relation {
        ACCEPTED, UNACCEPTED, NONE
    }

    /** The MemberID associated with the account in the connection. */
    private int mMemberID;
    /** The first name associated with the account in the connection. */
    private final String mFirstName;
    /** The last name associated with the account in the connection. */
    private final String mLastName;
    /** The Username associated with the account in the connection. */
    private final String mUsername;
    /** The Email associated with the account in the connection. */
    private final String mEmail;
    /** Whether the connection has been accepted by both users. */
    private Relation mIsAccepted;
    /** Whether the connection was sent by the current user. */
    private boolean mAmSender;

    /**
     * Constructs a new Connection.
     * @param memberID the MemberID associated with the connection
     * @param fName the first name associated with the account in the connection
     * @param lName the last name associated with the account in the connection
     * @param username the Username associated with the account in the connection
     * @param email the Email associated with the account in the connection
     * @param relation whether the connection has been accepted by both users
     * @param sender whether the connection was sent by the current user
     */
    public Connection(final int memberID, final String fName,
                      final String lName, final String username,
                      final String email, final Integer relation, final boolean sender) {
        mMemberID = memberID;
        mFirstName = fName;
        mLastName = lName;
        mUsername = username;
        mEmail = email;
        mAmSender = sender;
        // Determine relation by integer or null returned in row from database.
        if (relation == -1)
            mIsAccepted = Relation.NONE;
        else
            mIsAccepted = (relation.equals(1)) ? Relation.ACCEPTED : Relation.UNACCEPTED;
    }

    /**
     * Constructs a Connection from a Parcel.
     * @param in the Parcel with information to create the Connection from
     */
    protected Connection(Parcel in) {
        mMemberID =in.readByte();
        mFirstName = in.readString();
        mLastName = in.readString();
        mUsername = in.readString();
        mEmail = in.readString();
        mAmSender = in.readByte() != 0;
        int relation = in.readByte();
        if (relation == -1)
            mIsAccepted = Relation.NONE;
        else
            mIsAccepted = (relation == 1) ? Relation.ACCEPTED : Relation.UNACCEPTED;
    }

    /**
     * Gets the MemberID associated with the connection.
     * @return the MemberID
     */
    public int getMemberID() { return mMemberID; }

    /**
     * Gets the First Name associated with the connection.
     * @return the First Name
     */
    public String getFirstName() { return mFirstName; }

    /**
     * Gets the Last Name associated with the connection.
     * @return the Last Name
     */
    public String getLastName() { return mLastName; }

    /**
     * Gets the Username associated with the connection.
     * @return the Username
     */
    public String getUsername() { return mUsername; }

    /**
     * Gets the Email associated with the connection.
     * @return the Email
     */
    public String getEmail() { return mEmail; }

    /**
     * Returns <code>true</code> if the connection is mutually accepted.
     * @return whether the connection is accepted
     */
    public Relation getRelation() { return mIsAccepted; }

    /**
     * Returns <code>true</code> if the connection was sent by the current user.
     * @return whether the current user sent the connection
     */
    public boolean amSender() { return mAmSender; }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Connection))
            return false;
        return mMemberID == ((Connection) obj).mMemberID;
    }

    /** A Creator for use with producing Connections. */
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
        dest.writeInt(mMemberID);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mUsername);
        dest.writeString(mEmail);
        dest.writeInt(mAmSender ? 1 : 0);
        dest.writeInt(0);
    }

}
