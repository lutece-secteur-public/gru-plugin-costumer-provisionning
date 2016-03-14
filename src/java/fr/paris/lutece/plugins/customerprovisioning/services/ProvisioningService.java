/*
 * Copyright (c) 2002-2015, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.customerprovisioning.services;

import fr.paris.lutece.plugins.customerprovisioning.business.UserDTO;
import fr.paris.lutece.plugins.gru.business.customer.Customer;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.lang.StringUtils;


/**
 * The Class ProvisioningService.
 */
public final class ProvisioningService
{
    /**
     * Instantiates a new provisioning service.
     */
    private ProvisioningService(  )
    {
    }

    /**
     * Process guid cuid.
     *
     * @param strGuid the str guid
     * @param strCuid the str cuid
     * @param userDto the user dto
     * @return the customer
     */
    public static Customer processGuidCuid( String strGuid, String strCuid, UserDTO userDto )
    {
        Customer gruCustomer = null;

        // CASE 1 NOT CID
        if ( StringUtils.isEmpty( strCuid ) )
        {
            // CASE 1.1 : no cid and no guid:  break the flux and wait for a new flux with one of them
            if ( StringUtils.isEmpty( strGuid ) && ( userDto == null ) )
            {
                AppLogService.error( "Provisioning - Error : JSON doesnot contains any GUID nor Customer ID : " +
                    strCuid );

                //    return error( "grusupply - Error : JSON doesnot contains any GUID nor Customer ID" );
            } // CASE 1.2  : no cid and guid:  look for a mapping beween an existing guid
            else if ( ( userDto == null ) && !StringUtils.isEmpty( strGuid ) )
            {
                //gruCustomer = CustomerService.instance(  ).getCustomerByGuid( notif.getUserGuid(  ) );
                gruCustomer = getCustomerByGuid( strGuid );

                if ( gruCustomer == null )
                {
                    //                    gruCustomer = CustomerService.instance(  )
                    //                                                 .createCustomer( buildCustomer( 
                    //                                UserInfoService.instance(  ).getUserInfo( strTempGuid ), strTempGuid ) );
                    gruCustomer = createCustomerByGuid( strGuid );

                    AppLogService.info( "Provisioning - New user created into the GRU for the guid : " + strGuid +
                        " its customer id is : " + gruCustomer.getId(  ) );
                }
            }
            else
            {
                gruCustomer = createCustomerByGuid( userDto, strGuid );
            }
        } // CASE 2 : cid and (guid or no guid):  find customer info in GRU database
        else
        {
            //MUST CONTROL IF COSTUMER CUID IS NUMBER FORMAT, ELSE : java.lang.NumberFormatException: For input string:
            gruCustomer = getCustomerByCuid( strCuid );

            if ( gruCustomer == null )
            {
                AppLogService.error( "Provisioning - Error : No user found with the customer ID : " + strCuid );
            }
        }

        return gruCustomer;
    }

    /**
     * Get customer by Guid.
     *
     * @param strGuid the str guid
     * @return the customer by guid
     */
    public static Customer getCustomerByGuid( String strGuid )
    {
        Customer grusupplyCustomer = CustomerService.instance(  ).getCustomerByGuid( strGuid );

        return grusupplyCustomer;
    }

    /**
     *  Create customer by Guid.
     *
     * @param strGuid the str guid
     * @return the customer
     */
    public static Customer createCustomerByGuid( String strGuid )
    {
        UserDTO user = UserInfoService.instance(  ).getUserInfo( strGuid );
        Customer customer = CustomerService.instance(  ).createCustomer( buildCustomer( user, strGuid ) );

        return customer;
    }

    /**
     *  Create customer by Guid.
     *
     * @param userDto the user dto
     * @param strGuidFromTicket the str guid from ticket
     * @return the customer
     */
    public static Customer createCustomerByGuid( UserDTO userDto, String strGuidFromTicket )
    {
        Customer customer = CustomerService.instance(  ).createCustomer( buildCustomer( userDto, strGuidFromTicket ) );

        return customer;
    }

    /**
     * Get customer by Cuid.
     *
     * @param strCuid the str cuid
     * @return the customer by cuid
     */
    public static Customer getCustomerByCuid( String strCuid )
    {
        Customer grusupplyCustomer = CustomerService.instance(  ).getCustomerByCid( strCuid );

        return grusupplyCustomer;
    }

    /**
     * Builds the customer.
     *
     * @param user the user
     * @param strUserId the str user id
     * @return the customer
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

    /**
     * Sets the empty value when null value.
     *
     * @param value the value
     * @return the string
     */
    private static String setEmptyValueWhenNullValue( String value )
    {
        return ( StringUtils.isEmpty( value ) ) ? "" : value;
    }

    /**
     * Method which create a demand from Data base, a flux and GRU database
     *
     * @param gruCustomer
     * @return
     */

    //	    private static fr.paris.lutece.plugins.grusupply.business.Customer populateCustomerGRUToProvisioning( fr.paris.lutece.plugins.gru.business.customer.Customer gruCustomer )
    //	    {
    //	        if ( gruCustomer == null )
    //	        {
    //	            throw new NullPointerException(  );
    //	        }
    //
    //	        fr.paris.lutece.plugins.grusupply.business.Customer grusupplyCustomer = new fr.paris.lutece.plugins.grusupply.business.Customer(  );
    //	        grusupplyCustomer.setCustomerId( gruCustomer.getId(  ) );
    //	        grusupplyCustomer.setName( gruCustomer.getLastname(  ) );
    //	        grusupplyCustomer.setFirstName( gruCustomer.getFirstname(  ) );
    //	        grusupplyCustomer.setEmail( gruCustomer.getEmail(  ) );
    //	        grusupplyCustomer.setTelephoneNumber( gruCustomer.getMobilePhone(  ) );
    //
    //	        /*        grusupplyCustomer.setBirthday( gruCustomer.getBirthday(  ) );
    //	         grusupplyCustomer.setCivility( gruCustomer.getCivility(  ) );
    //	         grusupplyCustomer.setStreet( gruCustomer.getStreet(  ) );
    //	         grusupplyCustomer.setCityOfBirth( gruCustomer.getCityOfBirth(  ) );
    //	         grusupplyCustomer.setCity( gruCustomer.getCity(  ) );
    //	         grusupplyCustomer.setPostalCode( gruCustomer.getPostalCode(  ) );
    //	         */
    //	        grusupplyCustomer.setEmail( gruCustomer.getEmail(  ) );
    //	        grusupplyCustomer.setStayConnected( true );
    //
    //	        // TODO PROBLEME DE CHAMPS
    //	        return grusupplyCustomer;
    //	    }
}
