package com.example.sqlite2xl.model;

/**
 * Users class.
 */
public class Users {
    private String userId;
    private String contactPersonName;
    private String contactNumber;
    private byte[] contactPhoto;

    /**
     * Users constructor.
     *
     * @param contactPersonName person name
     * @param contactNumber contact number
     * @param contactPhoto contact photo
     */
    public Users(String contactPersonName, String contactNumber, byte[] contactPhoto) {
        this.contactPersonName = contactPersonName;
        this.contactNumber = contactNumber;
        this.contactPhoto = contactPhoto;
    }

    /**
     * Users constructor.
     *
     * @param userId userId
     * @param contactPersonName name
     * @param contactNumber contact number
     * @param contactPhoto contact photo
     */
    public Users(String userId, String contactPersonName, String contactNumber, byte[] contactPhoto) {
        this.userId = userId;
        this.contactPersonName = contactPersonName;
        this.contactNumber = contactNumber;
        this.contactPhoto = contactPhoto;
    }

    public String getUserId() {
        return userId;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public byte[] getContactPhoto() {
        return contactPhoto;
    }

    public void setContactPhoto(byte[] contactPhoto) {
        this.contactPhoto = contactPhoto;
    }
}
