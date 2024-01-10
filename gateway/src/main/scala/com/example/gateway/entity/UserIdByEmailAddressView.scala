package com.example.gateway.entity

import com.example.gateway.api.UserEmailRequest
import com.example.gateway.domain.UserLoginInfo
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class UserIdByEmailAddressView(context: ViewContext) extends AbstractUserIdByEmailAddressView {

  override def emptyState: UserLoginInfo =
    throw new UnsupportedOperationException("Not implemented yet, replace with your empty view state")

  override def registerRelation(
      state: UserLoginInfo,
      userLoginInfo: UserLoginInfo): UpdateEffect[UserLoginInfo] =
    throw new UnsupportedOperationException("Update handler for 'RegisterRelation' not implemented yet")

}
