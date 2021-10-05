<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->
<script>
    import {title} from "$lib/variables";

    title.set("About");
</script>
<section>
    <h1>About Crave!</h1>
    <p>Crave is designed to be the single best place to find the best deals for your local grocery stores.</p>
    <p>At Crave, we wouldn't exist without our customers. As such, we place tremendous value in our user experience,
        doing our best to ensure that contribute to our wonderful experience are able to do so with ease.</p>

    <div class="imgWrapper">
        <img src="/me.jpg" alt="Me!"/>
    </div>
    <p>My name is Travis Eggett. I am a Senior at Neumont College of Computer Science, studying Software
        Engineering. Crave was born of humble beginnings. My mom has always tried to save money where she could when
        shopping
        and came to me with bits and pieces of an idea to bring to life. While I was not experienced enough at the
        time, my knowledge has grown and I now consider myself able to take on that task.
    </p>
    <p>Though 10 weeks is not a lot of time to accomplish a task such as this, I am dedicated to bringing this
        product to life in such a way that at the very least, locals to the Utah area might be able to take
        advantage of the wonderful tool. My goal is to create a self-sustaining product which requires minimal
        intervention by myself to maintain and keep up to date.
    </p>
    <p>Inspiration was taken in part from <a href="https://www.gasbuddy.com/">GasBuddy</a>, a mobile application, which
        accomplishes the same "crowd-sourced pricing" aggregation specifically for gas. Groceries and other items that
        stores like Walmart carry are arguably harder to keep track of if not just for sheer number. Additionally, we
        must keep track of accurate product titles, descriptions, and prices so as to ensure that the user has the best
        experience when looking for their items.
    </p>
    <p>This all is no small task and I can only hope that, by the end of these 10 weeks, I have something
        comprehensive and polished to show for it!
    </p>
</section>
<style>
    section {
        display: flex;
        flex-direction: column;
        /*justify-content: center;*/
        align-items: center;
        flex: 1;
        flex-grow: 1;
    }

    h1 {
        font-size: 2rem;
    }

    p {
        text-align: center;
    }

    .imgWrapper {
        border-radius: 50%;
        max-width: 250px;
        max-height: 250px;
        overflow: hidden;
        box-shadow: 2px 5px 8px #333;
    }

    img {
        width: 100%;
    }
</style>