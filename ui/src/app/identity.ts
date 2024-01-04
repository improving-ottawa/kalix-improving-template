import {AppIdentity} from "../generated/com/example/gateway/domain/gateway_responses_pb";

export interface Identity {
    sub: string,
    exp: number,
    name: string,
    preferredName: string,
    familyName: string,
    givenName: string,
    middleName: string,
    email: string,
}

export const storeIdentityToken = (identity: AppIdentity, expTime: number) => {
    const identityObject: Identity = {
        sub: identity.getSub(),
        exp: expTime,
        name: identity.getName(),
        preferredName: identity.getPreferredName(),
        familyName: identity.getFamilyName(),
        givenName: identity.getGivenName(),
        middleName: identity.getMiddleName(),
        email: identity.getEmail()
    }

    const identityToken = JSON.stringify(identityObject)
    localStorage.setItem('identityToken', identityToken)
};

export const retrieveIdentity = () => {
    const identityToken = localStorage.getItem('identityToken')
    const identityObject = identityToken ? JSON.parse(identityToken) : {}

    function isIdentity(object: any): object is Identity {
        return (<Identity>object).sub !== undefined;
    }

    if (identityObject && isIdentity(identityObject)) {
        return identityObject as Identity;
    } else {
        return null
    }
}
