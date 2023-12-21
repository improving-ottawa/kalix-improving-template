import {getConfig} from "../../config";
import {GetUserRequest, GetUserResponse} from "../../../generated/com/example/gateway/domain/user_domain_pb";
import {decodedJwtToken, getCsrfToken, getGatewayClient} from "./clients";
import {CompleteLoginRequest} from "../../../generated/com/example/gateway/domain/gateway_commands_pb";
import {storeIdentityToken} from "../../identity";

export const sendBeginAuthenticationRequest = async () => {
    const config = await getConfig()
    return window.location.href = `${config.exampleApiBaseUrl}/oidc/auth?provider_id=local_keycloak&redirect_uri=/pricing`
}

export const sendCompleteAuthenticationRequest = async (code: string, state: string) => {
    const config = await getConfig()
    const client = await getGatewayClient()
    const request = new CompleteLoginRequest()
    request.setCode(code)
    request.setState(state)

    const response = await client.completeLogin(request, null)
    const csrfToken = response.getCsrfToken()
    const redirectTo = response.getRedirectUri()
    const identity = response.getIdentity()

    return new Promise<string>((resolve, reject) => {
        if (!csrfToken) {
            console.log("Did not receive `csrfToken`")
            reject()
        } else if (!redirectTo) {
            console.log("Did not receive `redirectTo`")
            reject()
        } else if (!decodedJwtToken()) {
            console.log("Did not receive `jwtToken`")
            reject()
        } else if (!identity) {
            console.log("Did not receive `identity`")
            reject()
        } else {
            const jwtExpiration = decodedJwtToken()?.exp ?? 0

            storeIdentityToken(identity, jwtExpiration)

            // Store the `csrfToken` in *session* storage
            sessionStorage.setItem('csrfToken', csrfToken)

            // Fixup the redirect URI to make it relative
            const relativeRedirect = redirectTo.replace(config.exampleUiBaseUri, "")
            resolve(relativeRedirect)
        }
    })
}

export function sendGetUserRequest(req: GetUserRequest) {
    var deadline = new Date();
    deadline.setSeconds(deadline.getSeconds() + 30);

    return new Promise<{ getUserResponse: GetUserResponse }>((resolve, reject) => {
        getGatewayClient().then(client => {
            console.log(getCsrfToken())
            client.getUser(req, {
                    deadline: deadline.getTime().toString(),
                    "X-CSRF-Token": getCsrfToken()
                },
                (err, response) => {
                    if (err) {
                        console.error(`Unexpected error sending get user: code = ${err.code}` +
                            `, message = "${err.message}"`)
                        reject(err)
                    } else {
                        const resp = {getUserResponse: response}
                        console.log(resp)
                        resolve(resp)
                    }
                })
        }).catch(err => {
            console.error(
                `Unexpected error creating client: code = ${err.code}` +
                `, message = "${err.message}"`
            )
            reject(err)
        })

    });

}

