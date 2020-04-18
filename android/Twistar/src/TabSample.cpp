
#include "TabSample.hpp"

#include <bb/cascades/Application>
#include <bb/cascades/QmlDocument>
#include <bb/cascades/AbstractPane>
#include <bps/navigator.h>
#include <bb/cascades/advertisement/Banner>
#include <bb/system/SystemDialog>

using namespace bb::cascades;
using namespace bb::system;
using namespace bb::cascades::advertisement;

TabSample::TabSample(bb::cascades::Application *app)
: QObject(app)
{

	searchfile = QString(QDir::homePath() + "/search.xml");
	nonefile = QString(QDir::homePath() + "/none.xml");
	qdpm = new QDeclarativePropertyMap();
	qdpm->insert("searchfile", QVariant("file://" + searchfile));
	qdpm->insert("nonefile", QVariant("file://" + nonefile));
	mgr = new QNetworkAccessManager();
	m_bbmUpdate = 0;

	if (!(QFile::exists(searchfile))) {
		QFile file("app/native/assets/models/search.xml");
		file.copy(searchfile);
	}

	// Registers the banner for QML
	qmlRegisterType<bb::cascades::advertisement::Banner>("bb.cascades.advertisement", 1, 0, "Banner");

    QmlDocument *qml = QmlDocument::create("asset:///main.qml").parent(this);
    qml->setContextProperty("app", this);
    qml->setContextProperty("qdpm", qdpm);

    AbstractPane *root = qml->createRootObject<AbstractPane>();
    app->setScene(root);

    m_context = new bb::platform::bbm::Context(QUuid("12078cdb-5884-4899-847e-c28d6f0f7bbb"));
    if (m_context->registrationState() != bb::platform::bbm::RegistrationState::Allowed) {
    	connect(m_context, SIGNAL(registrationStateUpdated (bb::platform::bbm::RegistrationState::Type)), this, SLOT(registrationStateUpdated (bb::platform::bbm::RegistrationState::Type)));
    	m_context->requestRegisterApplication();
    }

}

void TabSample::launchBrowser(QString url) {

	navigator_invoke(url.toStdString().c_str(), 0);

}

void TabSample::continueXML(void) {

    QString provider;

    switch(curr_depth) {
    case 1:
    	provider = "http://www.automechtic.web44.net/search.php";
    	break;
    case 2:
    	provider = "http://automechtic.yzi.me/search.php";
    	break;
    default:
    	provider = "http://www-automechtic.rhcloud.com/search.php";
    	break;
    }

    QUrl url(provider);

	url.addQueryItem("filter", curr_str);
	QNetworkRequest request(url);
	request.setHeader(QNetworkRequest::ContentTypeHeader, "text/plain");
	QNetworkReply* reply = mgr->post(request, url.encodedQuery());
	connect(reply, SIGNAL(finished()), this, SLOT(onProcessReply()));

}

void TabSample::startXML(QString str, int depth) {

	curr_str = str;
	curr_depth = depth;
	continueXML();

}

void TabSample::displayXML(QString str) {

	QFile file(searchfile);
    if (file.open(QIODevice::WriteOnly | QIODevice::Text)) {
    	QTextStream out(&file);
    	out << str;
    	file.close();
    }

    emit complete(str);

}

void TabSample::onProcessReply(void) {

	QNetworkReply* reply = qobject_cast<QNetworkReply*>(sender());

	QString response = "";
    if (reply) {
        if (reply->error() == QNetworkReply::NoError) {
            const int available = reply->bytesAvailable();
            if (available > 0) {
                const QByteArray buffer(reply->readAll());
                response = QString::fromUtf8(buffer);
            }
        }
        reply->deleteLater();
    }
    response = response.trimmed();

    int x = response.indexOf("<root>");
    int y = response.indexOf("</root>");
    QString failed = "<root><failed/></root>";

    if ((x > -1) && (y > -1)) {

        QString cleaned = response.mid(x, (y - x) + 7);
        if (cleaned.indexOf("<root></root>") > -1) {
        	displayXML(failed);
        } else {
        	displayXML(cleaned);
        }

    } else {

    	if (curr_depth < 2) { // number of providers
    		curr_depth++;
    		continueXML();
    	} else {
    		// no network
    	}

    }

}

void TabSample::inviteUserToDownloadViaBBM() {
	if (m_context->registrationState() == bb::platform::bbm::RegistrationState::Allowed) {
		m_messageService->sendDownloadInvitation();
	} else {
		SystemDialog *bbmDialog = new SystemDialog("OK");
		bbmDialog->setTitle(tr("BBM Connection Error"));
		bbmDialog->setBody(tr("BBM is not currently connected. Please setup / sign-in to BBM to remove this message."));
		connect(bbmDialog, SIGNAL(finished(bb::system::SystemUiResult::Type)), this, SLOT(dialogFinished(bb::system::SystemUiResult::Type)));
		bbmDialog->show();
		return;
	}
}

void TabSample::updatePersonalMessage(const QString &message) {
	if (m_context->registrationState() == bb::platform::bbm::RegistrationState::Allowed) {
		m_userProfile->requestUpdatePersonalMessage(message);
	} else {
		SystemDialog *bbmDialog = new SystemDialog("OK");
		bbmDialog->setTitle(tr("BBM Connection Error"));
		bbmDialog->setBody(tr("BBM is not currently connected. Please setup / sign-in to BBM to remove this message."));
		connect(bbmDialog, SIGNAL(finished(bb::system::SystemUiResult::Type)), this, SLOT(dialogFinished(bb::system::SystemUiResult::Type)));
		bbmDialog->show();
		return;
	}
}

void TabSample::registrationStateUpdated(bb::platform::bbm::RegistrationState::Type state) {
	if (state == bb::platform::bbm::RegistrationState::Allowed) {
		m_messageService = new bb::platform::bbm::MessageService(m_context, this);
		m_userProfile = new bb::platform::bbm::UserProfile(m_context, this);
	} else if (state == bb::platform::bbm::RegistrationState::Unregistered) {
		m_context->requestRegisterApplication();
	}
}

int TabSample::value() {
	return m_bbmUpdate;
}

void TabSample::setValue(int i) {
	m_bbmUpdate = i;
	emit valueChanged(m_bbmUpdate);
}
