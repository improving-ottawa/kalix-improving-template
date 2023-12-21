import {Typography} from "@mui/material";
import React, {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {useAppDispatch} from "../../redux/hooks";
import {sendCompleteAuthenticationRequest} from "../../redux/api/authApi";

const OIDCCallback = () => {
    // const dispatch = useAppDispatch()
    const navigate = useNavigate()

    const queryParams = new URLSearchParams(window.location.search)

    const completeLoginEffect = () => {
        const code = queryParams.get('code')
        const state = queryParams.get('state')

        if (!code || !state) {
            console.log(`Invalid query params - code: ${code}, state: ${state}`)
            navigate("/")
        } else {
            const completeLoginAsync = async() => {
                const redirectUri = await sendCompleteAuthenticationRequest(code, state)
                navigate(redirectUri)
            }

            completeLoginAsync()
        }
    }

    useEffect(completeLoginEffect)

    return <Typography> Redirecting after successful login... </Typography>
};

export default OIDCCallback;
