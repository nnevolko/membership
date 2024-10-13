package integrations.turnitin.com.membersearcher.service;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.MembershipList;
import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MembershipService {
	@Autowired
	private MembershipBackendClient membershipBackendClient;

	/**
	 * Method to fetch all memberships with their associated user details included.
	 * This method calls out to the php-backend service and fetches all memberships,
	 * it then calls to fetch all users and
	 * associates them with their corresponding membership.
	 *
	 * @return A CompletableFuture containing a fully populated MembershipList object.
	 */

	public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {

		CompletableFuture<MembershipList> membershipFuture = membershipBackendClient.fetchMemberships();
		CompletableFuture<UserList> usersFuture = membershipBackendClient.fetchUsers();
		CompletableFuture<MembershipList> updatedFuture =
				membershipFuture.thenCombine(usersFuture, (membershipList, userList) -> {
					return associateUsersWithMemberships(membershipList, userList);
			});

		return	updatedFuture;
		
	}

	public MembershipList associateUsersWithMemberships(MembershipList membershipList, UserList userList){
		Map<String, User> userMap = userList.getUsers().stream()
				.collect(Collectors.toMap(User::getId, user -> user));

		membershipList.getMemberships().stream().forEach(membership-> {
			User user = userMap.get(membership.getUserId());
			if (user != null) {
				membership.setUser(user);
			}
		});
		return membershipList;
	}

}