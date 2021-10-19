<script>
    import {formSubmit} from "$lib/util";
    import {title} from "$lib/variables";

    title.set("Register");

    let error;
    let success = false;
    let registerForm;
    const register = () => {
        formSubmit(registerForm, (data) => {
            if (data.error) {
                error = data.message.message;
                return;
            }


        });
    };

    //TODO Hook up registration form properly
</script>

<div class="full vcenter hcenter">
    {#if success}
        <div>
            <h1 class="success">&check; Successfully registered!</h1>
            <a href="/" class="button">Back Home</a>
        </div>
    {:else}
        <h1>Please fill out the form!</h1>
        <form bind:this={registerForm} id="registerForm">
            {#if error}
                <div class="error">{error}</div>
            {/if}
            <label for="username">Username</label>
            <input id="username" name="username" placeholder="Username" type="text"/>

            <label for="email">Email</label>
            <input id="email" name="email" placeholder="Email" type="text"/>

            <label for="password">Password</label>
            <input id="password" name="password" placeholder="Password" type="text"/>

            <label for="confPassword">Confirm Password</label>
            <input id="confPassword" name="confPassword" placeholder="Confirm Password" type="text"/>

            <a class="button" on:click={register}>Register</a>
        </form>
    {/if}
</div>

<style>
    form {
        display: flex;
        flex-direction: column;
        width: 50%;
        margin: 0 auto;
    }

    h1.success {
        color: green;
    }

    div.full {
        flex-direction: column;
    }
</style>