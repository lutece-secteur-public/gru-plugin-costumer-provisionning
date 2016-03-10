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
package fr.paris.lutece.plugins.costumerprovisionning.services;

import fr.paris.lutece.plugins.costumerprovisionning.business.UserDTO;
import fr.paris.lutece.plugins.gru.business.customer.Customer;
import fr.paris.lutece.portal.service.util.AppLogService;

import org.apache.commons.lang.StringUtils;


public class ProvisionningService
{
    public static Customer processGuidCuidTicketing( String strGuidFromTicket, String strCidFromTicket, UserDTO userDto )
    {
        fr.paris.lutece.plugins.costumerprovisionning.business.UserDTO userDtoOpenAm = null;
        fr.paris.lutece.plugins.gru.business.customer.Customer gruCustomer = null;

        if ( StringUtils.isEmpty( strCidFromTicket ) )
        {
            if ( !StringUtils.isEmpty( strGuidFromTicket ) )
            {
                //if guid is provided => we try to retrieve linked customer
                gruCustomer = getCustomerByGuid( strGuidFromTicket );
                userDtoOpenAm = UserInfoService.instance(  ).getUserInfo( strGuidFromTicket );
            }

            if ( gruCustomer == null )
            {
                //customer is unknown / not found => we create it
                if ( userDtoOpenAm == null )
                {
                    userDtoOpenAm = userDto;
                }

                gruCustomer = createCustomerByGuid( strGuidFromTicket );
                //create customer
                //  gruCustomer = CustomerService.instance(  ).createCustomer( buildCustomer( userDto, strGuidFromTicket ) );
                AppLogService.info( "New user created the guid : <" + strGuidFromTicket + "> its customer id is : <" +
                    gruCustomer.getId(  ) + ">" );
            }

            //update CID
            //            ticket.setCustomerId( String.valueOf( gruCustomer.getId(  ) ) );
            //            TicketHome.update( ticket );
        }
        else
        {
            if ( StringUtils.isEmpty( strGuidFromTicket ) )
            {
                if ( StringUtils.isNumeric( strCidFromTicket ) )
                {
                    // CASE : cid but no guid:  find customer info in GRU database => try to retrieve guid from customer
                    // gruCustomer = CustomerService.instance(  ).getCustomerByCid( strCidFromTicket );
                    gruCustomer = getCustomerByCuid( strCidFromTicket );
                }
                else
                {
                    AppLogService.error( "Provided customerId is not numeric: <" + strCidFromTicket + ">" );
                }

                if ( gruCustomer == null )
                {
                    AppLogService.info( "No guid found for user cid : <" + strCidFromTicket + ">" );
                }
            }
        }

        return gruCustomer;
    }

    public static Customer processGuidCuid( String strGuid, String strCuid )
    {
        Customer gruCustomer = null;

        // CASE 1 NOT CID
        if ( StringUtils.isEmpty( strCuid ) )
        {
            // CASE 1.1 : no cid and no guid:  break the flux and wait for a new flux with one of them
            if ( StringUtils.isEmpty( strGuid ) )
            {
                AppLogService.error( "Provionning - Error : JSON doesnot contains any GUID nor Customer ID : " +
                    strCuid );

                //    return error( "grusupply - Error : JSON doesnot contains any GUID nor Customer ID" );
            } // CASE 1.2  : no cid and guid:  look for a mapping beween an existing guid
            else
            {
                //gruCustomer = CustomerService.instance(  ).getCustomerByGuid( notif.getUserGuid(  ) );
                gruCustomer = getCustomerByGuid( strGuid );

                if ( gruCustomer == null )
                {
                    //                    gruCustomer = CustomerService.instance(  )
                    //                                                 .createCustomer( buildCustomer( 
                    //                                UserInfoService.instance(  ).getUserInfo( strTempGuid ), strTempGuid ) );
                    gruCustomer = createCustomerByGuid( strGuid );

                    AppLogService.info( "Provionning - New user created into the GRU for the guid : " + strGuid +
                        " its customer id is : " + gruCustomer.getId(  ) );
                }
            }
        } // CASE 2 : cid and (guid or no guid):  find customer info in GRU database
        else
        {
            //MUST CONTROL IF COSTUMER CUID IS NUMBER FORMAT, ELSE : java.lang.NumberFormatException: For input string:
            gruCustomer = getCustomerByCuid( strCuid );

            if ( gruCustomer == null )
            {
                AppLogService.error( "Provionning - Error : No user found with the customer ID : " + strCuid );
            }
        }

        return gruCustomer;
    }

    /**Get costumer by Guid
     * */
    public static Customer getCustomerByGuid( String strGuid )
    {
        Customer grusupplyCustomer = CustomerService.instance(  ).getCustomerByGuid( strGuid );

        return grusupplyCustomer;
    }

    /** Create costumer by Guid
     * */
    public static Customer createCustomerByGuid( String strGuid )
    {
        UserDTO user = UserInfoService.instance(  ).getUserInfo( strGuid );
        Customer costumer = CustomerService.instance(  ).createCustomer( buildCustomer( user, strGuid ) );

        return costumer;
    }

    /**CGet costumer by Cuid
     * */
    public static Customer getCustomerByCuid( String strCuid )
    {
        Customer grusupplyCustomer = CustomerService.instance(  ).getCustomerByCid( strCuid );

        return grusupplyCustomer;
    }

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

    /**
     * Method which create a demand from Data base, a flux and GRU database
     *
     * @param gruCustomer
     * @return
     */

    //	    private static fr.paris.lutece.plugins.grusupply.business.Customer populateCustomerGRUToProvisionning( fr.paris.lutece.plugins.gru.business.customer.Customer gruCustomer )
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
