import {getConfig} from "../../config";
import {GetUserRequest, GetUserResponse} from "../../../generated/com/example/gateway/domain/user_domain_pb";
import {getCsrfToken, getGatewayClient} from "./clients";
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
    const expTime = response.getSessionExpiration()
    const redirectTo = response.getRedirectUri()
    const identity = response.getIdentity()

    const errorHandler = (reject: (reason?: any) => void, name: string) => {
        const error = `Did not receive '${name}'`
        console.log(error)
        reject(error)
    }
    return new Promise<string>((resolve, reject) => {
        if (!csrfToken) {
            errorHandler(reject, "csrfToken")
        } else if (!redirectTo) {
            errorHandler(reject, "redirectTo")
        } else if (!identity) {
            errorHandler(reject, "identity")
        } else {
            // Store the App identity and session expiration time together
            storeIdentityToken(identity, expTime)

            // Store the `csrfToken` in *session* storage
            sessionStorage.setItem('csrfToken', csrfToken)

            // Fixup the redirect URI to make it relative
            const relativeRedirect = redirectTo.replace(config.exampleUiBaseUri, "")
            resolve(relativeRedirect)
        }
    })
}

export function sendGetUserRequest(req: GetUserRequest) {
    const deadline = new Date();
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

