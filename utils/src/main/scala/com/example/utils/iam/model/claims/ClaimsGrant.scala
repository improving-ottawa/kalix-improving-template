package com.example.utils.iam.model.claims

import com.example.utils.iam.model.IamPrincipal

/**
  * Claims granted to a user.
  *
  * @param principal          The user principal
  * @param access             The access claim
  * @param maybeRefreshClaim  The refresh claim (optional)
  * @param maybeIdClaim       The id claim (optional)
  */
case class ClaimsGrant(principal: IamPrincipal, access: AccessClaim, maybeRefreshClaim: Option[RefreshClaim], maybeIdClaim: Option[IdentityClaim])
