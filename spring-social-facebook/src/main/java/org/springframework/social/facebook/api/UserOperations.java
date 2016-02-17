/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.facebook.api;

import org.springframework.social.ApiException;
import org.springframework.social.MissingAuthorizationException;

import java.util.List;



public interface UserOperations {
	
	/**
	 * Retrieves the profile for the authenticated user.
	 * @return the user's profile information.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 * @throws MissingAuthorizationException if FacebookTemplate was not created with an access token.
	 */
	FacebookProfile getUserProfile();
	
	/**
	 * Retrieves the profile for the specified user.
	 * @param userId the Facebook user ID to retrieve profile data for.
	 * @return the user's profile information.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 */
	FacebookProfile getUserProfile(String userId);

	/**
	 * Retrieves the user's profile image. Returns the image in Facebook's "normal" type.
	 * @return an array of bytes containing the user's profile image.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 * @throws MissingAuthorizationException if FacebookTemplate was not created with an access token.
	 */
	byte[] getUserProfileImage();

	/**
	 * Retrieves the user's profile image. Returns the image in Facebook's "normal" type.
	 * @param userId the Facebook user ID.
	 * @return an array of bytes containing the user's profile image.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 */
	byte[] getUserProfileImage(String userId);

	/**
	 * Retrieves the user's profile image.
	 * @param imageType the image type (eg., small, normal, large. square)
	 * @return an array of bytes containing the user's profile image.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 * @throws MissingAuthorizationException if FacebookTemplate was not created with an access token.
	 */
	byte[] getUserProfileImage(ImageType imageType);

	/**
	 * Retrieves the user's profile image.
	 * @param userId the Facebook user ID.
	 * @param imageType the image type (eg., small, normal, large. square)
	 * @return an array of bytes containing the user's profile image.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 */
	byte[] getUserProfileImage(String userId, ImageType imageType);

	/**
	 * Get the URL of the user's profile image.
	 * @param userId the Facebook user ID.
	 * @return the user's profile image URL.
	 */
	String getUserProfileImageUrl(String userId);

	/**
	 * Get the URL of the user's profile image.
	 * @param userId the Facebook user ID.
	 * @param imageType the image type (eg., small, normal, large. square)
	 * @return the user's profile image URL.
	 */
	String getUserProfileImageUrl(String userId, ImageType imageType);

	/**
	 * Retrieves a list of permissions that the application has been granted for the authenticated user.
	 * @return the permissions granted for the user.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 * @throws MissingAuthorizationException if FacebookTemplate was not created with an access token.
	 */
	List<String> getUserPermissions();

	/**
	 * Retrieves the user's or page's cover photo.
	 * @return the cover photo containing the image source.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 */
	CoverPhoto getCoverPhoto();

	/**
	 * Retrieves the user's or page's cover photo.
	 * @param facebookId the Facebook user ID.
	 * @return the cover photo containing the image source.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 */
	CoverPhoto getCoverPhoto(String facebookId);


	/**
	 * Searches for users.
	 * @param query the search query (e.g., "Michael Scott")
	 * @return a list of {@link Reference}s, each representing a user who matched the given query.
	 * @throws ApiException if there is an error while communicating with Facebook.
	 * @throws MissingAuthorizationException if FacebookTemplate was not created with an access token.
	 */
	PagedList<Reference> search(String query);
}
