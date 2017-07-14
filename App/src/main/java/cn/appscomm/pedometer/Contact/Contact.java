package cn.appscomm.pedometer.Contact;

import java.util.ArrayList;

/**
 * 联系人
 * 
 * @author FY_Zeng
 * 
 */
public class Contact {
	/** 联系人ID */
	private String id;
	/** 姓名 */
	private String name;
	/** 电话号码 */
	private ArrayList<PhoneNumber> phoneNumList;
	/** Email */
	private ArrayList<Email> Emails;
	/** IM */
	private ArrayList<String> imList;
	/** 地址 */
	private ArrayList<Address> address;
	/** 组织 */
	private ArrayList<Organizations> organizationsList;
	/** 备注 */
	private ArrayList<String> notesList;
	/** 昵称 */
	private ArrayList<String> nicknamesList;

	/** 获取联系人ID */
	public String getId() {
		return id;
	}

	/** 设置联系人ID */
	public void setId(String id) {
		this.id = id;
	}

	/** 获取名字 */
	public String getName() {
		return name;
	}

	/** 设置名字 */
	public void setName(String name) {
		this.name = name;
	}

	/** 获取电话号码 */
	public ArrayList<PhoneNumber> getPhoneNumList() {
		return phoneNumList;
	}

	/** 设置电话号码 */
	public void setPhoneNumList(ArrayList<PhoneNumber> phoneNumList) {
		this.phoneNumList = phoneNumList;
	}

	/** 获取邮箱 */
	public ArrayList<Email> getEmails() {
		return Emails;
	}

	/** 设置邮箱 */
	public void setEmails(ArrayList<Email> emails) {
		Emails = emails;
	}

	/** 获取IM */
	public ArrayList<String> getImList() {
		return imList;
	}

	/** 设置IM */
	public void setImList(ArrayList<String> imList) {
		this.imList = imList;
	}

	/** 获取地址 */
	public ArrayList<Address> getAddress() {
		return address;
	}

	/** 设置地址 */
	public void setAddress(ArrayList<Address> address) {
		this.address = address;
	}

	/** 获取组织 */
	public ArrayList<Organizations> getOrganizationsList() {
		return organizationsList;
	}

	/** 设置组织 */
	public void setOrganizationsList(ArrayList<Organizations> organizationsList) {
		this.organizationsList = organizationsList;
	}

	/** 获取备注 */
	public ArrayList<String> getNotesList() {
		return notesList;
	}

	/** 设置备注 */
	public void setNotesList(ArrayList<String> notesList) {
		this.notesList = notesList;
	}

	/** 获取昵称 */
	public ArrayList<String> getNicknamesList() {
		return nicknamesList;
	}

	/** 设置昵称 */
	public void setNicknamesList(ArrayList<String> nicknamesList) {
		this.nicknamesList = nicknamesList;
	}

}
