package com.improving.utils.iam

import kalix.scalasdk.action.Action

trait KalixJwtAuthorization extends Action {

  def requiresRoles[R](role: String, otherRoles: String*)(actionBody: => Action.Effect[R]): Action.Effect[R]

}
