import {
    Typography
} from "@mui/material";
import React, {useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {getUser, selectAuthState} from "../../redux/slices/authSlice";
import Cookies from "cookies-ts";
import {decodedJwtToken} from "../../redux/api/clients";
import moment from "moment/moment";
import {useAppDispatch, useAppSelector} from "../../redux/hooks";

const AuthRedirect = () => {
    const dispatch = useAppDispatch()
    const navigate = useNavigate()

    const loginState = useAppSelector(selectAuthState)

    const initializeStorageForAuth = () => {
        const cookies = new Cookies()

        const csrfToken = cookies.get('csrfToken')
        console.log("Storing (and deleting) CSRF token...")
        csrfToken ? sessionStorage.setItem('csrfToken', csrfToken) : console.log("csrfToken is invalid")
        cookies.remove('csrfToken')

        const redirectToElement = document.getElementById('redirectTo')
        console.log("Redirecting to target page: " + redirectToElement?.innerText)
        redirectToElement?.click()

        const jwt = decodedJwtToken()
        if (jwt?.exp && jwt.exp < moment.now()) {
            dispatch(getUser())
        } else {
            console.log(jwt?.exp)
            navigate("/login")
        }
    }

    useEffect(() => initializeStorageForAuth())


    return <Typography> Redirecting after successful authorization... </Typography>
};

export default AuthRedirect;
