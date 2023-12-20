import {GetUserRequest, GetUserResponse} from "../../../generated/com/example/gateway/domain/user_domain_pb";
import {csrfHeader, getGatewayClient} from "./clients";

export const sendBeginAuthenticationRequest = () => {
    window.location.href = "http://localhost:8010/oidc/auth?provider_id=local_keycloak&redirect_uri=http://localhost:3000/pricing"
}

export function sendGetUserRequest(req: GetUserRequest) {
    const deadline = new Date();
    deadline.setSeconds(deadline.getSeconds() + 30);

    return new Promise<{ getUserResponse: GetUserResponse }>((resolve, reject) => {
        getGatewayClient().then(client => {
            console.log(csrfHeader())
            client.getUser(req, {
                    deadline: deadline.getTime().toString(),
                    authorization: csrfHeader()
                },
                (err, response) => {
                    if (err) {
                        console.error(`Unexpected error sending admin login link: code = ${err.code}` +
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

