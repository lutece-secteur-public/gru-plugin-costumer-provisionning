package fr.paris.lutece.plugins.costumerprovisionning.services;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.costumerprovisionning.business.UserDTO;
import fr.paris.lutece.plugins.gru.business.customer.Customer;
import fr.paris.lutece.portal.service.util.AppLogService;

public class ProvisionningService {

	
	/**
	 *  processGuidCuid
	 * @param strGuid
	 * @param strCuid
	 * @param userDto
	 * @return Customer
	 */
	public static Customer processGuidCuid(String strGuid, String strCuid,UserDTO userDto)
	{
		
		
		AppLogService.error( "\n" + "Provionning - Info : GUID : " + strGuid + "\n" );
		AppLogService.error( "Provionning - Info : CID : " + strCuid  + "\n");
		AppLogService.error( "Provionning - Info : user : " + userDto + "\n" );
		
		Customer gruCustomer=null;
		
		   // CASE 1 NOT CID
        if ( strCuid == null || StringUtils.isEmpty( strCuid ) )
        {
            // CASE 1.1 : no cid and no guid:  break the flux and wait for a new flux with one of them
            if ( (strCuid == null || StringUtils.isEmpty( strGuid )) && userDto == null)
            {
            	
                AppLogService.error( "Provionning - Error : JSON doesnot contains any GUID nor Customer ID : " + strCuid  );
  
            } // CASE 1.2  : no cid and guid:  look for a mapping beween an existing guid
            else if(!StringUtils.isEmpty( strGuid ))
            {
            	
            	AppLogService.error( "Provionning - Info : CAS 1.2.1" + "\n" );
                gruCustomer =getCustomerByGuid(strGuid);

                if ( gruCustomer == null )
                {
                    
                    gruCustomer = createCustomerByGuid(strGuid);
                    
                    AppLogService.info( "Provionning - New user created into the GRU for the guid : " + strGuid +" its customer id is : " + gruCustomer.getId(  ) );
                }
            }else if (userDto != null)
            {
            	
            	gruCustomer = createCustomerByGuid(userDto,strGuid);
            	AppLogService.error( "Provionning - Info : CAS 1.2.2" + "\n" );
            }
        } // CASE 2 : cid and (guid or no guid):  find customer info in GRU database
        else if(StringUtils.isNumeric( strCuid ) )
        {
        	//MUST CONTROL IF COSTUMER CUID IS NUMBER FORMAT, ELSE : java.lang.NumberFormatException: For input string:
        	AppLogService.error( "Provionning - Info : CAS 2" + "\n" );
            gruCustomer = getCustomerByCuid(strCuid);

            if ( gruCustomer == null )
            {
             
            	  AppLogService.error( "Provionning - Error : No user found with the customer ID : " + strCuid  );
            }
        }
		
        
        return gruCustomer;
	}
	
	/**
	 * Get costumer by Guid
	 * @param strGuid
	 * @return
	 */
	public static Customer getCustomerByGuid(String strGuid)
	{		
		Customer grusupplyCustomer = CustomerService.instance(  ).getCustomerByGuid( strGuid );
		return  grusupplyCustomer;
	}
	
	
	/**
	 * Create costumer by Guid
	 * @param strGuid
	 * @return
	 */
	public static  Customer createCustomerByGuid(String strGuid)
	{		
		UserDTO user = UserInfoService.instance(  ).getUserInfo( strGuid );	
		Customer costumer=CustomerService.instance(  ).createCustomer( buildCustomer( user, strGuid ) );	
		return  costumer;
	}
	
	/**
	 * Create costumer by Guid
	 * @param userDto
	 * @param strGuidFromTicket
	 * @return
	 */
	public static  Customer createCustomerByGuid(UserDTO userDto, String strGuidFromTicket)
	{		
		
		Customer costumer=CustomerService.instance(  ).createCustomer( buildCustomer( userDto, strGuidFromTicket ) );	
		return  costumer;
	}
	
	
	/**
	 * Get costumer by Cuid
	 * @param strCuid
	 * @return
	 */
	
	public static Customer getCustomerByCuid(String strCuid)
	{
		
		Customer grusupplyCustomer = CustomerService.instance(  ).getCustomerByCid( strCuid );
		
	    return  grusupplyCustomer;
	}
	
	/**
	 * 
	 * @param user
	 * @param strUserId
	 * @return
	 */
	
	private static Customer buildCustomer( UserDTO user, String strUserId )
	{
	    fr.paris.lutece.plugins.gru.business.customer.Customer gruCustomer = new fr.paris.lutece.plugins.gru.business.customer.Customer(  );
	    gruCustomer.setFirstname( setEmptyValueWhenNullValue( user.getFirstname(  ) ) );
	    gruCustomer.setLastname( setEmptyValueWhenNullValue( user.getLastname(  ) ) );
	    gruCustomer.setEmail( setEmptyValueWhenNullValue( user.getEmail(  ) ) );
	    gruCustomer.setAccountGuid( setEmptyValueWhenNullValue( strUserId ) );
	    gruCustomer.setAccountLogin( setEmptyValueWhenNullValue( user.getEmail(  ) ) );
	    gruCustomer.setMobilePhone( setEmptyValueWhenNullValue( user.getTelephoneNumber(  ) ) );
	    gruCustomer.setExtrasAttributes( "NON RENSEIGNE" );

	        return gruCustomer;
	}
	   
	private static String setEmptyValueWhenNullValue( String value )
	{
	     return ( StringUtils.isEmpty( value ) ) ? "" : value;
	}
	   

}
