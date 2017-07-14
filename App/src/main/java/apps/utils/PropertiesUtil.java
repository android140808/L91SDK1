package apps.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import android.content.Context;
import android.util.Log;

/**
 * properties文件读写的辅助类。
 * 
 * @author kxz
 *
 */
public class PropertiesUtil {
	private static final String dataPathPrefix="/data/data/";
	private static final String pathSeparator="/";
	//
	private static Properties props = null;
	//private static Properties props = PropertiesWithCrypto();
	
	public PropertiesUtil() {
		props = new Properties();
	}
	
	public PropertiesUtil(List<String> cryptofields) {
		props = new PropertiesWithCrypto( cryptofields );
	}
	
	/**
	 * 对放在activity的对应包package目录下的*.properties文件的操作(非res下)。
	 * 
	 * @param pkgPath
	 * @param fileName
	 */
	public void initPkgPropFile( String pkgPath, String fileName ){
		String fullPath = dataPathPrefix.concat(pkgPath).concat(pathSeparator).concat(fileName);
		
		try {
			File fProp = new File( fullPath );  
			if( !fProp.exists() ) fProp.createNewFile();
			InputStream in = new FileInputStream( fProp );   
			
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();  
			Logger.e( this.getClass().getName(), e.getMessage() );
		}
	}
	
	/**
	 * 对res资源目录下的*.properties文件的操作。
	 * 用法：如读取res/raw, initResPropFile("/res/raw/", "rdp.properties")
	 * 
	 * @param c
	 * @param fileName
	 */
	public void initResPropFile( String resPrefix, String fileName ){
		try {   
			//通过class获取setting.properties的FileInputStream   
			InputStream in = PropertiesUtil.class.getResourceAsStream( resPrefix.concat( fileName) );
			props.load(in);  
		} catch (Exception e) {
			e.printStackTrace();  
			Logger.e( this.getClass().getName(), e.getMessage() );
		} 
	}
	
	/**
	 * 对res资源目录下的*.properties文件的操作。
	 * 用法：如读取res/raw, initResPropFile("/res/raw/", "rdp.properties")
	 * 
	 * @param c
	 * @param fileName
	 */
	public void initResRawPropFile( Context c, int propFileId ){
		try {   
			if (c != null) {
				//通过class获取setting.properties的FileInputStream   
				InputStream in = c.getResources().openRawResource( propFileId );  
				
				props.load(new InputStreamReader(in, "utf-8"));
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();  
			Logger.e( this.getClass().getName(), e.getMessage() );
		} 
	}
	
	/**
	 * 对assets目录下的*.properties文件的操作。
	 * 
	 * @param c Activity的当前context
	 * @param relFilePathName 相对于/assests/下的其它路径与文件名
	 */
	public void initAssetsPropFile( Context c, String relFilePathName ){
		//Assets: AssetManager assetManager = getAssets();
	 	//Raw: InputStream inputStream = getResources().openRawResource(R.raw.demo);
		
		try {   
			//通过activity中的context读取*.properties的FileInputStream
			InputStream in = c.getAssets().open( relFilePathName );
			
			props.load(in);  
		} catch (Exception e) {
			e.printStackTrace();  
			//
			Logger.e( this.getClass().getName(), e.getMessage() );
		} 
	}
	
	/**
	 * 读取一个属性值。
	 * 
	 * @param propName
	 * @return
	 */
	public String readProperty( String propName ) {		
		return props.getProperty( propName );
	}
	
	public Properties getPropsObj() {
		return this.props;
	}
}
