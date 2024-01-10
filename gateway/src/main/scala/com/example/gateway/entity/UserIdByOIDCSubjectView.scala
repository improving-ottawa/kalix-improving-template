package com.example.gateway.entity

import com.example.gateway.api.ForeignIdentityRequest
import com.example.gateway.domain.ForeignIdentityUserIdRelation
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class UserIdByOIDCSubjectView(context: ViewContext) extends AbstractUserIdByOIDCSubjectView {

  override def emptyState: ForeignIdentityUserIdRelation =
    throw new UnsupportedOperationException("Not implemented yet, replace with your empty view state")

  override def registerRelation(
    state: ForeignIdentityUserIdRelation,
    foreignIdentityUserIdRelation: ForeignIdentityUserIdRelation
  ): UpdateEffect[ForeignIdentityUserIdRelation] =
    throw new UnsupportedOperationException("Update handler for 'RegisterRelation' not implemented yet")

}
