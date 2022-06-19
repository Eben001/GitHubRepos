package com.devtides.githubrepos.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devtides.githubrepos.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody

class MainViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val tokenLD = MutableLiveData<String>()
    val errorLd = MutableLiveData<String>()
    val reposLD = MutableLiveData<List<GithubRepo>>()
    val prsLD = MutableLiveData<List<GithubPR>>()
    val postCommentsLD = MutableLiveData<Boolean>()

    val commentsLD = MutableLiveData<List<GithubComment>>()

    fun getToken(clientId: String, clientSecret: String, code: String) {
        compositeDisposable.add(
            GithubService.getUnauthorizedApi().getAuthToken(clientId, clientSecret, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<GithubToken>() {
                    override fun onSuccess(t: GithubToken) {
                        tokenLD.value = t.accessToken
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        errorLd.value = "Cannot Load token"
                    }

                })

        )
    }

    fun onLoadRepositories(token: String) {
        compositeDisposable.add(
            GithubService.getAuthorizedApi(token).getAllRepos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<List<GithubRepo>>() {
                    override fun onSuccess(value: List<GithubRepo>) {
                        reposLD.value = value
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        errorLd.value = "Cannot load repositories"
                    }
                })
        )
    }
    fun onLoadPRs(token:String, owner:String?, repository:String?){
        if (owner!=null && repository!=null){
            compositeDisposable.add(
                GithubService.getAuthorizedApi(token).getPRs(owner, repository)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object:DisposableSingleObserver<List<GithubPR>>(){
                        override fun onSuccess(value: List<GithubPR>) {
                            prsLD.value = value
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            errorLd.value = "Cannot load Pulls"
                        }
                    })
            )
        }
    }
    fun onLoadComments(token:String, owner: String?, repository: String?, pullNumber:String?){
        if(owner!=null && repository!=null && pullNumber!=null){
            compositeDisposable.add(
                GithubService.getAuthorizedApi(token).getComments(owner, repository, pullNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<GithubComment>>() {
                        override fun onSuccess(value: List<GithubComment>) {
                            commentsLD.value = value
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            errorLd.value = "Cannot load Comments for this issue"
                        }
                    })
            )
        }
    }

    fun onPostComment(token: String, repo:GithubRepo, pullNumber: String?, content: GithubComment){
        if(repo.owner.login!=null && repo.name!=null && pullNumber!=null){
            compositeDisposable.add(
                GithubService.getAuthorizedApi(token).postComment(repo.owner.login, repo.name,
                pullNumber, content)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object :DisposableSingleObserver<ResponseBody>(){
                        override fun onSuccess(value: ResponseBody) {
                            postCommentsLD.value = true

                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            errorLd.value = "Cannot Create comment"
                        }
                    })
            )
        }
    }



    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}